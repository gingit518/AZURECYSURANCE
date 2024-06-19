package com.cyberintech.vrisk.server.model.dto.qualitative_question;

import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Vendor Questions Holder View
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-02-06
 */
@Setter
@Getter
public class VendorQuestionsView {

	@Schema
	private Long vendorId;

	@Schema
	private String name;

	@Schema
	private String metricDomainCode;

	@Schema
	private List<QualitativeQuestionWithAnswersViewDTO> questions;

	/**
	 * Default constructor
	 */
	public VendorQuestionsView() {
	}

	@java.beans.ConstructorProperties({"vendorId", "metricDomainCode", "questions"})
	public VendorQuestionsView(Long vendorId, String metricDomainCode, List<QualitativeQuestionWithAnswersViewDTO> questions) {
		this.vendorId = vendorId;
		this.metricDomainCode = metricDomainCode;
		this.questions = questions;
	}

	public VendorQuestionsView(Long vendorId, String name, String metricDomainCode, List<QualitativeQuestionWithAnswersViewDTO> questions) {
		this.vendorId = vendorId;
		this.name = name;
		this.metricDomainCode = metricDomainCode;
		this.questions = questions;
	}

	public VendorQuestionsView(Organizations vendor, String metricDomainCode, List<QualitativeQuestionWithAnswersViewDTO> questions) {
		if (vendor != null) {
			this.vendorId = vendor.getId();
			this.name = vendor.getName();
		}
		this.metricDomainCode = metricDomainCode;
		this.questions = questions;
	}

}
