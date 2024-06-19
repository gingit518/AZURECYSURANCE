package com.cyberintech.vrisk.server.model.data;

import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Implementation of Question Vendor Filtering Logic
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-10-28
 */
@NoArgsConstructor
@Setter
@Getter
public class QuestionVendorAdvancedFilter extends QuestionVendorFilter {

	@JsonIgnore
	private Long riskModelId;

	@JsonIgnore
	private Boolean isTechnologyVendor;

	@JsonIgnore
	private Boolean isSystemVendor;

	@JsonIgnore
	private Boolean isServiceVendor;

	@JsonIgnore
	private Boolean isInternal;

	@JsonIgnore
	private Boolean ignoreInternal;

	/**
	 * Static constructor
	 *
	 * @param questionVendorFilter
	 * @return
	 */
	public static QuestionVendorAdvancedFilter of (QuestionVendorFilter questionVendorFilter) {
		QuestionVendorAdvancedFilter result = new QuestionVendorAdvancedFilter();
		result.setVendorId(questionVendorFilter.getVendorId());
		result.setMetricDomain(questionVendorFilter.getMetricDomain());
		result.setQuestionTypes(questionVendorFilter.getQuestionTypes());

		return result;
	}
}
