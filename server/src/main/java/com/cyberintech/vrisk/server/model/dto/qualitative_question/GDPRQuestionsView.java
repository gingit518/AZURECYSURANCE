package com.cyberintech.vrisk.server.model.dto.qualitative_question;

import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * System Questions Holder View
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-02-20
 */
@Setter
@Getter
public class GDPRQuestionsView {

	@Schema
	private Long systemId;

	@Schema
	private Long vendorId;

	@Schema
	private String name;

	@Schema
	private String metricDomainCode;

	@Schema
	private List<GDPRQualitativeQuestionViewDTO> questions;

	/**
	 * Defaault constructor
	 */
	public GDPRQuestionsView() {
	}

//	@java.beans.ConstructorProperties({"systemId", "metricDomainCode", "questions"})
//	public GDPRQuestionsView(Long systemId, String metricDomainCode, List<GDPRQualitativeQuestionViewDTO> questions) {
//		this.systemId = systemId;
//		this.vendorId = systemId;
//		this.metricDomainCode = metricDomainCode;
//		this.questions = questions;
//	}

	public GDPRQuestionsView(Systems system, String metricDomainCode, List<GDPRQualitativeQuestionViewDTO> questions) {
		if (system != null) {
			this.systemId = system.getId();
			this.name = system.getName();
		}
		this.metricDomainCode = metricDomainCode;
		this.questions = questions;
	}

	public GDPRQuestionsView(Organizations vendor, String metricDomainCode, List<GDPRQualitativeQuestionViewDTO> questions) {
		if (vendor != null) {
			this.vendorId = vendor.getId();
			this.name = vendor.getName();
		}
		this.metricDomainCode = metricDomainCode;
		this.questions = questions;
	}

}
