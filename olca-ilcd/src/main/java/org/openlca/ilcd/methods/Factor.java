
package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.DataDerivation;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.RecommendationLevel;
import org.openlca.ilcd.commons.UncertaintyDistribution;
import org.openlca.ilcd.commons.annotations.Label;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CharacterisationFactorType", propOrder = {
		"referenceToFlowDataSet",
		"location",
		"exchangeDirection",
		"meanValue",
		"minimumValue",
		"maximumValue",
		"uncertaintyDistributionType",
		"relativeStandardDeviation95In",
		"dataDerivationTypeStatus",
		"deviatingRecommendation",
		"referencesToDataSource",
		"generalComment",
		"other"
})
public class Factor implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(required = true)
	protected Ref referenceToFlowDataSet;

	protected String location;

	@XmlElement(required = true)
	protected ExchangeDirection exchangeDirection;

	protected double meanValue;

	protected Double minimumValue;

	protected Double maximumValue;

	protected UncertaintyDistribution uncertaintyDistributionType;

	protected BigDecimal relativeStandardDeviation95In;

	protected DataDerivation dataDerivationTypeStatus;

	protected RecommendationLevel deviatingRecommendation;

	protected DataSourceReferenceList referencesToDataSource;

	@Label
	protected List<LangString> generalComment;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected Other other;

	@XmlAnyAttribute
	private Map<QName, String> otherAttributes = new HashMap<>();

	/**
	 * Gets the value of the referenceToFlowDataSet property.
	 * 
	 * @return possible object is {@link Ref }
	 * 
	 */
	public Ref getReferenceToFlowDataSet() {
		return referenceToFlowDataSet;
	}

	/**
	 * Sets the value of the referenceToFlowDataSet property.
	 * 
	 * @param value
	 *            allowed object is {@link Ref }
	 * 
	 */
	public void setReferenceToFlowDataSet(Ref value) {
		this.referenceToFlowDataSet = value;
	}

	/**
	 * Gets the value of the location property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Sets the value of the location property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLocation(String value) {
		this.location = value;
	}

	/**
	 * Gets the value of the exchangeDirection property.
	 * 
	 * @return possible object is {@link ExchangeDirection }
	 * 
	 */
	public ExchangeDirection getExchangeDirection() {
		return exchangeDirection;
	}

	/**
	 * Sets the value of the exchangeDirection property.
	 * 
	 * @param value
	 *            allowed object is {@link ExchangeDirection }
	 * 
	 */
	public void setExchangeDirection(ExchangeDirection value) {
		this.exchangeDirection = value;
	}

	/**
	 * Gets the value of the meanValue property.
	 * 
	 */
	public double getMeanValue() {
		return meanValue;
	}

	/**
	 * Sets the value of the meanValue property.
	 * 
	 */
	public void setMeanValue(double value) {
		this.meanValue = value;
	}

	/**
	 * Gets the value of the minimumValue property.
	 * 
	 * @return possible object is {@link Double }
	 * 
	 */
	public Double getMinimumValue() {
		return minimumValue;
	}

	/**
	 * Sets the value of the minimumValue property.
	 * 
	 * @param value
	 *            allowed object is {@link Double }
	 * 
	 */
	public void setMinimumValue(Double value) {
		this.minimumValue = value;
	}

	/**
	 * Gets the value of the maximumValue property.
	 * 
	 * @return possible object is {@link Double }
	 * 
	 */
	public Double getMaximumValue() {
		return maximumValue;
	}

	/**
	 * Sets the value of the maximumValue property.
	 * 
	 * @param value
	 *            allowed object is {@link Double }
	 * 
	 */
	public void setMaximumValue(Double value) {
		this.maximumValue = value;
	}

	/**
	 * Gets the value of the uncertaintyDistributionType property.
	 * 
	 * @return possible object is {@link UncertaintyDistribution }
	 * 
	 */
	public UncertaintyDistribution getUncertaintyDistributionType() {
		return uncertaintyDistributionType;
	}

	/**
	 * Sets the value of the uncertaintyDistributionType property.
	 * 
	 * @param value
	 *            allowed object is {@link UncertaintyDistribution }
	 * 
	 */
	public void setUncertaintyDistributionType(UncertaintyDistribution value) {
		this.uncertaintyDistributionType = value;
	}

	/**
	 * Gets the value of the relativeStandardDeviation95In property.
	 * 
	 * @return possible object is {@link BigDecimal }
	 * 
	 */
	public BigDecimal getRelativeStandardDeviation95In() {
		return relativeStandardDeviation95In;
	}

	/**
	 * Sets the value of the relativeStandardDeviation95In property.
	 * 
	 * @param value
	 *            allowed object is {@link BigDecimal }
	 * 
	 */
	public void setRelativeStandardDeviation95In(BigDecimal value) {
		this.relativeStandardDeviation95In = value;
	}

	/**
	 * Gets the value of the dataDerivationTypeStatus property.
	 * 
	 * @return possible object is {@link DataDerivation }
	 * 
	 */
	public DataDerivation getDataDerivationTypeStatus() {
		return dataDerivationTypeStatus;
	}

	/**
	 * Sets the value of the dataDerivationTypeStatus property.
	 * 
	 * @param value
	 *            allowed object is {@link DataDerivation }
	 * 
	 */
	public void setDataDerivationTypeStatus(DataDerivation value) {
		this.dataDerivationTypeStatus = value;
	}

	/**
	 * Gets the value of the deviatingRecommendation property.
	 * 
	 * @return possible object is {@link RecommendationLevel }
	 * 
	 */
	public RecommendationLevel getDeviatingRecommendation() {
		return deviatingRecommendation;
	}

	/**
	 * Sets the value of the deviatingRecommendation property.
	 * 
	 * @param value
	 *            allowed object is {@link RecommendationLevel }
	 * 
	 */
	public void setDeviatingRecommendation(RecommendationLevel value) {
		this.deviatingRecommendation = value;
	}

	/**
	 * Gets the value of the referencesToDataSource property.
	 * 
	 * @return possible object is {@link DataSourceReferenceList }
	 * 
	 */
	public DataSourceReferenceList getReferencesToDataSource() {
		return referencesToDataSource;
	}

	/**
	 * Sets the value of the referencesToDataSource property.
	 * 
	 * @param value
	 *            allowed object is {@link DataSourceReferenceList }
	 * 
	 */
	public void setReferencesToDataSource(DataSourceReferenceList value) {
		this.referencesToDataSource = value;
	}

	/**
	 * Gets the value of the generalComment property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the generalComment property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getGeneralComment().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Label }
	 * 
	 * 
	 */
	public List<LangString> getGeneralComment() {
		if (generalComment == null) {
			generalComment = new ArrayList<>();
		}
		return this.generalComment;
	}

	/**
	 * Gets the value of the other property.
	 * 
	 * @return possible object is {@link Other }
	 * 
	 */
	public Other getOther() {
		return other;
	}

	/**
	 * Sets the value of the other property.
	 * 
	 * @param value
	 *            allowed object is {@link Other }
	 * 
	 */
	public void setOther(Other value) {
		this.other = value;
	}

	/**
	 * Gets a map that contains attributes that aren't bound to any typed
	 * property on this class.
	 * 
	 * <p>
	 * the map is keyed by the name of the attribute and the value is the string
	 * value of the attribute.
	 * 
	 * the map returned by this method is live, and you can add new attribute by
	 * updating the map directly. Because of this design, there's no setter.
	 * 
	 * 
	 * @return always non-null
	 */
	public Map<QName, String> getOtherAttributes() {
		return otherAttributes;
	}

}
