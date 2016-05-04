package org.openlca.core.matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.dbtables.PicoAllocationFactor;
import org.openlca.core.matrix.dbtables.PicoExchange;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongDoubleHashMap;

class AllocationIndex {

	private MatrixCache cache;
	private TechIndex productIndex;
	private AllocationMethod method;

	/**
	 * Used for physical and economic allocation: directly stores the the
	 * allocation factors for the given process-products.
	 */
	private HashMap<LongPair, Double> productFactors;

	/**
	 * Used for causal allocation: stores the relation process-product ->
	 * exchange -> allocation factor.
	 */
	private HashMap<LongPair, TLongDoubleHashMap> exchangeFactors;

	public static AllocationIndex create(TechIndex productIndex,
			AllocationMethod method, MatrixCache cache) {
		return new AllocationIndex(productIndex, method, cache);
	}

	private AllocationIndex(TechIndex productIndex, AllocationMethod method,
			MatrixCache cache) {
		this.method = method;
		this.productIndex = productIndex;
		this.cache = cache;
		List<PicoAllocationFactor> factors = loadFactors();
		for (PicoAllocationFactor factor : factors)
			index(factor);
	}

	private List<PicoAllocationFactor> loadFactors() {
		try {
			List<PicoAllocationFactor> factors = new ArrayList<>();
			Map<Long, List<PicoAllocationFactor>> factorMap = cache
					.getAllocationCache().getAll(productIndex.getProcessIds());
			for (List<PicoAllocationFactor> list : factorMap.values())
				factors.addAll(list);
			return factors;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to load allocation factors from cache", e);
			return Collections.emptyList();
		}
	}

	private void index(PicoAllocationFactor factor) {
		LongPair processProduct = new LongPair(factor.processID,
				factor.productID);
		AllocationMethod _method = this.method;
		if (this.method == AllocationMethod.USE_DEFAULT)
			_method = cache.getProcessTable().getDefaultAllocationMethod(
					factor.processID);
		if (_method == null)
			return;
		switch (_method) {
		case CAUSAL:
			tryIndexCausal(processProduct, factor);
			break;
		case ECONOMIC:
			tryIndexForProduct(processProduct, factor, _method);
			break;
		case PHYSICAL:
			tryIndexForProduct(processProduct, factor, _method);
			break;
		default:
			break;
		}
	}

	private void tryIndexCausal(LongPair processProduct,
			PicoAllocationFactor factor) {
		if (factor.method != AllocationMethod.CAUSAL
				|| factor.exchangeID == null)
			return;
		if (exchangeFactors == null)
			exchangeFactors = new HashMap<>();
		TLongDoubleHashMap map = exchangeFactors.get(processProduct);
		if (map == null) {
			// 1.0 is the default value -> means no allocation
			map = new TLongDoubleHashMap(Constants.DEFAULT_CAPACITY,
					Constants.DEFAULT_LOAD_FACTOR,
					Constants.DEFAULT_LONG_NO_ENTRY_VALUE, 1d);
			exchangeFactors.put(processProduct, map);
		}
		map.put(factor.exchangeID, factor.value);
	}

	private void tryIndexForProduct(LongPair processProduct,
			PicoAllocationFactor factor, AllocationMethod method) {
		if (factor.method != method)
			return;
		if (method != AllocationMethod.ECONOMIC
				&& method != AllocationMethod.PHYSICAL)
			return;
		if (productFactors == null)
			productFactors = new HashMap<>();
		productFactors.put(processProduct, factor.value);
	}

	public double getFactor(LongPair processProduct,
			PicoExchange calcExchange) {
		if (!calcExchange.isInput && calcExchange.flowType == FlowType.PRODUCT_FLOW)
			return 1d; // TODO: this changes when we allow input-modelling
						// of waste-flows
		AllocationMethod _method = this.method;
		if (this.method == AllocationMethod.USE_DEFAULT)
			_method = cache.getProcessTable().getDefaultAllocationMethod(
					processProduct.getFirst());
		if (_method == null)
			return 1d;
		switch (_method) {
		case CAUSAL:
			return fetchCausal(processProduct, calcExchange);
		case ECONOMIC:
			return fetchForProduct(processProduct);
		case PHYSICAL:
			return fetchForProduct(processProduct);
		default:
			return 1d;
		}
	}

	private double fetchForProduct(LongPair processProduct) {
		if (productFactors == null)
			return 1d;
		Double factor = productFactors.get(processProduct);
		if (factor == null)
			return 1d;
		else
			return factor;
	}

	private double fetchCausal(LongPair processProduct, PicoExchange e) {
		if (exchangeFactors == null)
			return 1d;
		TLongDoubleHashMap map = exchangeFactors.get(processProduct);
		if (map == null)
			return 1d;
		return map.get(e.exchangeID); // default is 1.0
	}

}