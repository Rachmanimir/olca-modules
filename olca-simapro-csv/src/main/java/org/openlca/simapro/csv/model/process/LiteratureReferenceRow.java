package org.openlca.simapro.csv.model.process;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;
import org.openlca.simapro.csv.model.IDataRow;

public class LiteratureReferenceRow implements IDataRow {

	public String name;
	public String comment;

	@Override
	public void fill(String line, CsvConfig config) {
		String[] columns = CsvUtils.split(line, config);
		name = CsvUtils.get(columns, 0);
		comment = CsvUtils.get(columns, 1);
	}

	@Override
	public String toString() {
		return "LiteratureReferenceRow [name=" + name + "]";
	}

}
