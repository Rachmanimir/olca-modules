package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrices.FlowIndex;
import org.openlca.core.matrices.FormulaInterpreterBuilder;
import org.openlca.core.matrices.ImpactMatrix;
import org.openlca.core.matrices.ImpactMatrixBuilder;
import org.openlca.core.matrices.Inventory;
import org.openlca.core.matrices.InventoryBuilder;
import org.openlca.core.matrices.LongPair;
import org.openlca.core.matrices.ProductIndex;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.expressions.FormulaInterpreter;

/**
 * Helper methods for the calculators in this package.
 */
final class Calculators {

	private Calculators() {
	}

	static IMatrix createDemandVector(ProductIndex productIndex) {
		LongPair refProduct = productIndex.getRefProduct();
		int idx = productIndex.getIndex(refProduct);
		IMatrix demandVector = MatrixFactory.create(productIndex.size(), 1);
		demandVector.setEntry(idx, 0, productIndex.getDemand());
		return demandVector;
	}

	/**
	 * Creates a matrix with the impact assessment factors for the given method
	 * and flows.
	 */
	static ImpactMatrix createImpactMatrix(ImpactMethodDescriptor method,
			FlowIndex flowIndex, IDatabase database) {
		ImpactMatrixBuilder builder = new ImpactMatrixBuilder(database);
		ImpactMatrix matrix = builder.build(method.getId(), flowIndex);
		return matrix;
	}

	static Inventory createInventory(CalculationSetup setup, IDatabase database) {
		ProductSystem system = setup.getProductSystem();
		AllocationMethod method = setup.getAllocationMethod();
		if (method == null)
			method = AllocationMethod.NONE;
		ProductIndex productIndex = createProductIndex(system);
		InventoryBuilder inventoryBuilder = new InventoryBuilder(database);
		Inventory inventory = inventoryBuilder.build(productIndex, method);
		FormulaInterpreter interpreter = FormulaInterpreterBuilder.build(
				database, productIndex.getProcessIds());
		FormulaInterpreterBuilder
				.apply(setup.getParameterRedefs(), interpreter);
		inventory.setFormulaInterpreter(interpreter);
		return inventory;
	}

	/**
	 * Creates a product index from the given product system.
	 * 
	 * TODO: there is currently no check if the system is correctly defined.
	 */
	static ProductIndex createProductIndex(ProductSystem system) {
		Process refProcess = system.getReferenceProcess();
		Exchange refExchange = system.getReferenceExchange();
		Flow refFlow = refExchange.getFlow();
		LongPair refProduct = new LongPair(refProcess.getId(), refFlow.getId());
		double demand = system.getConvertedTargetAmount();
		ProductIndex index = new ProductIndex(refProduct, demand);
		for (ProcessLink link : system.getProcessLinks()) {
			long flow = link.getFlowId();
			long provider = link.getProviderId();
			long recipient = link.getRecipientId();
			LongPair processProduct = new LongPair(provider, flow);
			index.put(processProduct);
			LongPair input = new LongPair(recipient, flow);
			index.putLink(input, processProduct);
		}
		return index;
	}

}