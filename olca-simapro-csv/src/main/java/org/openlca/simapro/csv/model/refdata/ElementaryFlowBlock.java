package org.openlca.simapro.csv.model.refdata;

import java.util.List;

import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

public interface ElementaryFlowBlock {

	ElementaryFlowType type();

	List<ElementaryFlowRow> rows();

}
