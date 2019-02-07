package org.openlca.core.math;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.matrix.CostVector;
import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.openlca.expressions.FormulaInterpreter;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;

/**
 * Provides helper methods for creating matrix-like data structures that can be
 * used in calculations (but also exports, validations, etc.).
 */
public class DataStructures {

	private DataStructures() {
	}

	/**
	 * Creates a product index from the given product system.
	 */
	public static TechIndex createProductIndex(
			ProductSystem system, IDatabase db) {

		// initialize the TechIndex with the reference flow
		Exchange refExchange = system.referenceExchange;
		ProcessProduct refFlow = ProcessProduct.of(
				system.referenceProcess, refExchange.flow);
		TechIndex index = new TechIndex(refFlow);
		index.setDemand(ReferenceAmount.get(system));

		// initialize the fast descriptor maps
		ProductSystemDao sysDao = new ProductSystemDao(db);
		TLongObjectHashMap<ProductSystemDescriptor> systems = sysDao
				.descriptorMap();
		TLongObjectHashMap<ProcessDescriptor> processes = new ProcessDao(
				db).descriptorMap();
		TLongObjectHashMap<FlowDescriptor> flows = new FlowDao(
				db).descriptorMap();

		// the link queue may then contain links of sub-systems
		Queue<List<ProcessLink>> queue = new ArrayDeque<>();
		TLongHashSet handledSystems = new TLongHashSet();
		handledSystems.add(system.id);
		queue.add(system.processLinks);

		while (!queue.isEmpty()) {
			List<ProcessLink> links = queue.poll();
			for (ProcessLink link : links) {
				CategorizedDescriptor p = processes.get(link.providerId);
				if (p == null) {
					p = systems.get(link.providerId);
					if (p == null)
						continue;
					ProductSystem sub = sysDao.getForId(p.id);

					if (sub != null && !handledSystems.contains(sub.id)) {
						// add the sub-system to the tech-index
						handledSystems.add(sub.id);
						if (sub.referenceProcess != null
								&& sub.referenceExchange.flow != null) {
							ProcessProduct subRef = ProcessProduct.of(
									sub.referenceProcess,
									sub.referenceExchange.flow);
							index.put(subRef);
							queue.add(sub.processLinks);
						}
					}
				}
				FlowDescriptor flow = flows.get(link.flowId);
				if (flow == null)
					continue;

				// the tech-index checks for duplicates of products and links
				ProcessProduct provider = ProcessProduct.of(p, flow);
				index.put(provider);
				LongPair exchange = new LongPair(link.processId,
						link.exchangeId);
				index.putLink(exchange, provider);
			}
		}
		return index;
	}

	public static Inventory createInventory(ProductSystem system,
			MatrixCache cache) {
		TechIndex index = createProductIndex(system, cache.getDatabase());
		AllocationMethod method = AllocationMethod.USE_DEFAULT;
		return Inventory.build(cache, index, method);
	}

	public static Inventory createInventory(ProductSystem system,
			AllocationMethod allocationMethod, MatrixCache cache) {
		TechIndex index = createProductIndex(system, cache.getDatabase());
		return Inventory.build(cache, index, allocationMethod);
	}

	public static Inventory createInventory(
			CalculationSetup setup, MatrixCache cache) {
		ProductSystem system = setup.productSystem;
		AllocationMethod method = setup.allocationMethod;
		if (method == null)
			method = AllocationMethod.NONE;
		TechIndex productIndex = createProductIndex(system,
				cache.getDatabase());
		productIndex.setDemand(ReferenceAmount.get(setup));
		return Inventory.build(cache, productIndex, method);
	}

	/**
	 * Create the matrix data for the calculation of the given setup.
	 */
	public static MatrixData matrixData(CalculationSetup setup,
			IMatrixSolver solver, MatrixCache mcache) {
		IDatabase db = mcache.getDatabase();
		Inventory inventory = createInventory(
				setup, mcache);
		FormulaInterpreter interpreter = interpreter(
				db, setup, inventory.techIndex);
		MatrixData data = inventory.createMatrix(
				solver, interpreter);
		if (setup.impactMethod != null) {
			ImpactTable impacts = ImpactTable.build(
					mcache, setup.impactMethod.id,
					inventory.flowIndex);
			data.impactMatrix = impacts.createMatrix(
					solver, interpreter);
			data.impactIndex = impacts.impactIndex;
		}
		if (setup.withCosts) {
			data.costVector = CostVector.build(
					inventory, db);
		}
		return data;
	}

	public static Set<Long> parameterContexts(CalculationSetup setup,
			TechIndex techIndex) {
		HashSet<Long> set = new HashSet<>();
		if (setup != null && setup.impactMethod != null) {
			set.add(setup.impactMethod.id);
		}
		if (techIndex != null) {
			set.addAll(techIndex.getProcessIds());
		}
		return set;
	}

	public static FormulaInterpreter interpreter(IDatabase db,
			CalculationSetup setup, TechIndex techIndex) {
		return ParameterTable.interpreter(db,
				parameterContexts(setup, techIndex),
				setup.parameterRedefs);
	}
}
