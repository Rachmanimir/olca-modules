package org.openlca.core.library;

import java.util.HashMap;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.TechIndex;

public class LibraryCalculator {

	private final IDatabase db;
	private final LibraryDir libDir;
	private MatrixData foregroundData;

	/**
	 * Contains the loaded libraries: library ID -> library.
	 */
	private final HashMap<String, Library> libraries = new HashMap<>();

	/**
	 * The product indices for the loaded libraries.
	 */
	private final HashMap<String, TechIndex> libTechIndices = new HashMap<>();

	/**
	 * The elem. flow indices of the loaded libraries.
	 */
	private final HashMap<String, FlowIndex> libFlowIndices = new HashMap<>();

	public LibraryCalculator(IDatabase db, LibraryDir libDir) {
		this.db = db;
		this.libDir = libDir;
	}

	public LibraryResult calculate(MatrixData foregroundData) {
		this.foregroundData = foregroundData;

		var result = new LibraryResult();
		result.techIndex = techIndex();
		result.flowIndex = flowIndex();

		return result;
	}

	private TechIndex techIndex() {

		var indexF = foregroundData.techIndex;
		var index = new TechIndex(indexF.getRefFlow());
		index.setDemand(indexF.getDemand());

		indexF.each((pos, product) -> {
			var lib = product.getLibrary();
			if (lib.isEmpty()) {
				index.put(product);
			} else {
				libraries.computeIfAbsent(lib.get(),
						libID -> libDir.get(libID).orElseThrow(
								() -> new RuntimeException(
										"Could not load library " + libID)));

			}
		});

		libraries.keySet().forEach(libID -> {
			var lib = libraries.get(libID);
			var libIndex = lib.syncProducts(db).orElseThrow(
					() -> new RuntimeException(
							"Could not load product index of " + libID));
			libTechIndices.put(libID, libIndex);
			libIndex.each((_pos, product) -> index.put(product));
		});

		return index;
	}

	/**
	 * Creates the combined flow index of the library result. Note that
	 * this may be if this is a result without elementary flows.
	 */
	private FlowIndex flowIndex() {

		// initialize the flow index with the foreground
		// index if present
		FlowIndex index = null;
		var indexF = foregroundData.flowIndex;
		if (indexF != null) {
			index = indexF.isRegionalized
					? FlowIndex.createRegionalized()
					: FlowIndex.create();
			index.putAll(indexF);
		}

		// extend the flow index with the flow indices
		// of used libraries.
		for (var entry : libraries.entrySet()) {
			var libID = entry.getKey();
			var lib = entry.getValue();
			var libIdx = lib.syncElementaryFlows(db).orElse(null);
			if (libIdx == null)
				continue;
			if (index == null) {
				index = libIdx.isRegionalized
						? FlowIndex.createRegionalized()
						: FlowIndex.create();
			}
			index.putAll(libIdx);
			libFlowIndices.put(libID, libIdx);
		}

		return index;
	}
}
