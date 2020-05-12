package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.InterpreterException;

/**
 * In openLCA, parameters can be defined in different scopes: global, process,
 * or LCIA method. The parameter name can be used in formulas and, thus, need to
 * conform to a specific syntax. Within a scope the parameter name should be
 * unique (otherwise the evaluation is not deterministic). There are two types
 * of parameters in openLCA: input parameters and dependent parameters. An input
 * parameter can have an optional uncertainty distribution but not a formula. A
 * dependent parameter can (should) have a formula (where also other parameters
 * can be used) but no uncertainty distribution.
 */
@Entity
@Table(name = "tbl_parameters")
public class Parameter extends CategorizedEntity {

	/**
	 * The scope of the parameter (global, process, LCIA method).
	 */
	@Column(name = "scope")
	@Enumerated(EnumType.STRING)
	public ParameterScope scope = ParameterScope.GLOBAL;

	/**
	 * Indicates whether the parameter is an input parameter.
	 */
	@Column(name = "is_input_param")
	public boolean isInputParameter;

	/**
	 * The value of the parameter.
	 */
	@Column(name = "value")
	public double value;

	/**
	 * The formula of the parameter (only valid for dependent parameters).
	 */
	@Column(name = "formula")
	public String formula;

	/**
	 * The uncertainty distribution of the parameter value (only valid for input
	 * parameters).
	 */
	@Embedded
	public Uncertainty uncertainty;

	@Override
	public Parameter clone() {
		Parameter clone = new Parameter();
		Util.cloneRootFields(this, clone);
		clone.formula = formula;
		clone.isInputParameter = isInputParameter;
		clone.scope = scope;
		if (uncertainty != null)
			clone.uncertainty = uncertainty.clone();
		clone.value = value;
		return clone;
	}

	@Override
	public String toString() {
		return "Parameter [formula=" + formula + ", name=" + name
				+ ", type=" + scope + "]";
	}

}
