package org.openlca.core.matrix;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongIntHashMap;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class FlowIndex2 {

	public final boolean isRegionalized;
	private final TLongIntHashMap index;
	private final HashMap<LongPair, Integer> regIndex;
	private final ArrayList<IndexFlow> flows = new ArrayList<>();

	private FlowIndex2(boolean isRegionalized) {
		this.isRegionalized = isRegionalized;
		if (isRegionalized) {
			index = null;
			regIndex = new HashMap<>();
		} else {
			index = new TLongIntHashMap(
					Constants.DEFAULT_CAPACITY,
					Constants.DEFAULT_LOAD_FACTOR,
					-1L, // no entry key
					-1); // no entry value
			regIndex = null;
		}
	}

	public static FlowIndex2 create() {
		return new FlowIndex2(false);
	}

	public static FlowIndex2 createRegionalized() {
		return new FlowIndex2(true);
	}

	public int size() {
		return flows.size();
	}

	public boolean isEmpty() {
		return flows.isEmpty();
	}

	public IndexFlow at(int i) {
		if (i < 0 || i >= flows.size())
			return null;
		return flows.get(i);
	}

	public int of(FlowDescriptor flow) {
		if (flow == null)
			return -1;
		if (isRegionalized)
			return of(flow.id, 0L);
		return index.get(flow.id);
	}

	public int of(FlowDescriptor flow, LocationDescriptor loc) {
		if (flow == null)
			return -1;
		if (isRegionalized)
			return of(flow.id, loc != null ? loc.id : 0L);
		return index.get(flow.id);
	}

	public int of(long flowID) {
		if (isRegionalized)
			return of(flowID, 0L);
		return index.get(flowID);
	}

	public int of(long flowID, long locationID) {
		if (isRegionalized) {
			Integer idx = regIndex.get(LongPair.of(flowID, locationID));
			return idx == null ? -1 : idx;
		}
		return index.get(flowID);
	}

	public boolean contains(FlowDescriptor flow) {
		return of(flow) >= 0;
	}

	public boolean contains(FlowDescriptor flow, LocationDescriptor location) {
		return of(flow, location) >= 0;
	}

	public boolean contains(long flowID) {
		return of(flowID) >= 0;
	}

	public boolean contains(long flowID, long locationID) {
		return of(flowID, locationID) >= 0;
	}

	public int putInput(FlowDescriptor flow) {
		return put(flow, null, true);
	}

	public int putInput(FlowDescriptor flow, LocationDescriptor location) {
		return put(flow, location, true);
	}

	public int putOutput(FlowDescriptor flow) {
		return put(flow, null, false);
	}

	public int putOutput(FlowDescriptor flow, LocationDescriptor location) {
		return put(flow, location, false);
	}

	private int put(FlowDescriptor flow,
	                LocationDescriptor location,
	                boolean isInput) {
		if (flow == null)
			return -1;

		int idx = flows.size();

		// check if the flow should be added
		if (isRegionalized) {
			long locID = location == null ? 0L : location.id;
			LongPair p = LongPair.of(flow.id, locID);
			Integer i = regIndex.get(p);
			if (i != null)
				return i;
			regIndex.put(p, idx);
		} else {
			int i = index.get(flow.id);
			if (i > -1)
				return i;
			index.put(flow.id, idx);
		}

		// create and add the index flow
		IndexFlow f = new IndexFlow();
		f.index = idx;
		f.flow = flow;
		f.location = isRegionalized ? location : null;
		f.isInput = isInput;
		flows.add(f);
		return idx;
	}

	public void each(Consumer<IndexFlow> fn) {
		if (fn == null)
			return;
		for (IndexFlow f : flows) {
			fn.accept(f);
		}
	}

	public Set<IndexFlow> flows() {
		return new HashSet<>(flows);
	}

}