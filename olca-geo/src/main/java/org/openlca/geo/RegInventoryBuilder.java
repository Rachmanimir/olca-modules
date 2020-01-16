package org.openlca.geo;

import java.util.HashSet;
import java.util.List;

import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.database.LocationDao;
import org.openlca.core.matrix.AllocationIndex;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.InventoryConfig;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.RegFlowIndex;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.cache.ExchangeTable;
import org.openlca.core.matrix.cache.FlowTable;
import org.openlca.core.matrix.format.MatrixBuilder;
import org.openlca.core.matrix.uncertainties.UMatrix;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.results.SimpleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegInventoryBuilder {

	private final InventoryConfig conf;
	private final TechIndex techIndex;
	private final FlowTable flows;
	private final TLongObjectHashMap<LocationDescriptor> locations;

	private RegFlowIndex flowIndex;
	private AllocationIndex allocationIndex;

	private MatrixBuilder techBuilder;
	private MatrixBuilder enviBuilder;
	private UMatrix techUncerts;
	private UMatrix enviUncerts;
	private double[] costs;

	public RegInventoryBuilder(InventoryConfig conf) {
		this.conf = conf;
		this.techIndex = conf.techIndex;
		this.flows = FlowTable.create(conf.db);
		this.locations = new LocationDao(conf.db).descriptorMap();

		techBuilder = new MatrixBuilder();
		enviBuilder = new MatrixBuilder();
		if (conf.withUncertainties) {
			techUncerts = new UMatrix();
			enviUncerts = new UMatrix();
		}
		if (conf.withCosts) {
			costs = new double[conf.techIndex.size()];
		}
	}

	public MatrixData build() {
		if (conf.allocationMethod != null
				&& conf.allocationMethod != AllocationMethod.NONE) {
			allocationIndex = AllocationIndex.create(
					conf.db, techIndex, conf.allocationMethod);
		}

		// create the index of elementary flows; when the system has sub-systems
		// we add the flows of the sub-systems to the index; note that there
		// can be elementary flows that only occur in a sub-system
		flowIndex = new RegFlowIndex();
		if (conf.subResults != null) {
			for (SimpleResult sub : conf.subResults.values()) {
				if (sub.regFlowIndex == null)
					continue;
				sub.regFlowIndex.each((i, flow, location) -> {
					if (!flowIndex.contains(flow, location)) {
						if (sub.isInput(flow)) {
							flowIndex.putInput(flow, location);
						} else {
							flowIndex.putOutput(flow, location);
						}
					}
				});
			}
		}

		// fill the matrices
		fillMatrices();
		int n = techIndex.size();
		int m = flowIndex.size();
		techBuilder.minSize(n, n);
		enviBuilder.minSize(m, n);

		// return the matrix data
		MatrixData data = new MatrixData();
		data.techIndex = techIndex;
		data.regFlowIndex = flowIndex;
		data.techMatrix = techBuilder.finish();
		data.enviMatrix = enviBuilder.finish();
		data.techUncertainties = techUncerts;
		data.enviUncertainties = enviUncerts;
		data.costVector = costs;
		return data;
	}

	private void fillMatrices() {
		try {
			// fill the matrices with process data
			ExchangeTable exchanges = new ExchangeTable(conf.db);
			exchanges.each(techIndex, exchange -> {
				List<ProcessProduct> products = techIndex
						.getProviders(exchange.processId);
				for (ProcessProduct product : products) {
					putExchangeValue(product, exchange);
				}
			});

			// now put the entries of the sub-system into the matrices
			HashSet<ProcessProduct> subSystems = new HashSet<>();
			techIndex.each((i, p) -> {
				if (p.process == null)
					return;
				if (p.process.type == ModelType.PRODUCT_SYSTEM) {
					subSystems.add(p);
				}
			});
			if (subSystems.isEmpty())
				return;

			// use the MatrixBuiler.set method here because there may
			// are stored LCI results that were mapped to the respective
			// columns
			for (ProcessProduct sub : subSystems) {

				int col = techIndex.getIndex(sub);
				SimpleResult r = conf.subResults.get(sub);
				if (r == null) {
					// TODO: log this error
					continue;
				}

				// add the link in the technology matrix
				double a = r.techIndex.getDemand();
				techBuilder.set(col, col, a);

				// add the LCI result
				if (r.regFlowIndex != null) {
					r.regFlowIndex.each((i, flow, location) -> {
						double b = r.getTotalFlowResult(flow, location);
						if (r.isInput(flow)) {
							b = -b;
						}
						enviBuilder.set(
								flowIndex.of(flow, location), col, b);
					});
				}

				// add costs
				if (conf.withCosts) {
					costs[col] = r.totalCosts;
				}
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to load exchanges from cache", e);
		}
	}

	private void putExchangeValue(ProcessProduct provider, CalcExchange e) {
		if (e.flowType == FlowType.ELEMENTARY_FLOW) {
			// elementary flows
			addIntervention(provider, e);
			return;
		}

		if ((e.isInput && e.flowType == FlowType.PRODUCT_FLOW)
				|| (!e.isInput && e.flowType == FlowType.WASTE_FLOW)) {
			if (techIndex.isLinked(LongPair.of(e.processId, e.exchangeId))) {
				// linked product input or waste output
				addProcessLink(provider, e);
			} else {
				// unlinked product input or waste output
				addIntervention(provider, e);
			}
			return;
		}

		if (provider.equals(e.processId, e.flowId)) {
			// the reference product or waste flow
			int idx = techIndex.getIndex(provider);
			add(idx, provider, techBuilder, e);
			return;
		}

		if (conf.allocationMethod == null
				|| conf.allocationMethod == AllocationMethod.NONE) {
			// non allocated output products or waste inputs
			addIntervention(provider, e);
		}
	}

	private void addProcessLink(ProcessProduct product, CalcExchange e) {
		LongPair exchange = LongPair.of(e.processId, e.exchangeId);
		ProcessProduct provider = techIndex.getLinkedProvider(exchange);
		int row = techIndex.getIndex(provider);
		add(row, product, techBuilder, e);
	}

	private void addIntervention(ProcessProduct provider, CalcExchange e) {
		int row = flowIndex.of(e.flowId, e.locationId);
		if (row < 0) {
			FlowDescriptor flow = flows.get(e.flowId);
			LocationDescriptor loc = locations.get(e.locationId);
			if (flow == null || loc == null)
				return;
			if (e.isInput) {
				row = flowIndex.putInput(flow, loc);
			} else {
				row = flowIndex.putOutput(flow, loc);
			}
		}
		add(row, provider, enviBuilder, e);
	}

	private void add(int row, ProcessProduct provider, MatrixBuilder matrix,
					 CalcExchange exchange) {

		int col = techIndex.getIndex(provider);
		if (row < 0 || col < 0)
			return;

		double allocationFactor = 1.0;
		if (allocationIndex != null && exchange.isAllocatable()) {
			allocationFactor = allocationIndex.get(
					provider, exchange.exchangeId);
		}

		double value = exchange.matrixValue(
				conf.interpreter, allocationFactor);
		matrix.add(row, col, value);

		if (conf.withCosts) {
			costs[col] += exchange.costValue(
					conf.interpreter, allocationFactor);
		}

		if (conf.withUncertainties) {
			if (matrix == techBuilder) {
				techUncerts.add(row, col, exchange, allocationFactor);
			}
			if (matrix == enviBuilder) {
				enviUncerts.add(row, col, exchange, allocationFactor);
			}
		}
	}

}