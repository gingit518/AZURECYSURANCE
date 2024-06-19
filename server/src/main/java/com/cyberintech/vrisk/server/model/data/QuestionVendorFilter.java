package com.cyberintech.vrisk.server.model.data;

import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Implementation of Name Filtering Logic
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-04
 */
@NoArgsConstructor
@Setter
@Getter
public class QuestionVendorFilter extends BaseFilter<Long> {

	@Schema
	private Long vendorId;

	@Schema
	private String metricDomain;

	private Boolean ignoreVendorSelection;

	private Boolean isTechnologyVendor;

	private Boolean isSystemVendor;

	private Boolean isServiceVendor;

	private Boolean isSelfAssessment;

	@JsonIgnore
	private List<VendorType> questionTypes;

}
