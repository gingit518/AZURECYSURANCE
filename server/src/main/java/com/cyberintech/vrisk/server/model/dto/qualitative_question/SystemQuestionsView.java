package com.cyberintech.vrisk.server.model.dto.qualitative_question;

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
 * @since    2019-04-19
 */
@Setter
@Getter
public class SystemQuestionsView {

	@Schema
	private Long systemId;

	@Schema
	private String name;

	@Schema
	private String metricDomainCode;

	@Schema
	private List<QualitativeQuestionWithAnswersViewDTO> questions;

	/**
	 * Defaault constructor
	 */
	public SystemQuestionsView() {
	}

	@java.beans.ConstructorProperties({"systemId", "metricDomainCode", "questions"})
	public SystemQuestionsView(Long systemId, String metricDomainCode, List<QualitativeQuestionWithAnswersViewDTO> questions) {
		this.systemId = systemId;
		this.metricDomainCode = metricDomainCode;
		this.questions = questions;
	}

	public SystemQuestionsView(Systems system, String metricDomainCode, List<QualitativeQuestionWithAnswersViewDTO> questions) {
		if (system != null) {
			this.systemId = system.getId();
			this.name = system.getName();
		}
		this.metricDomainCode = metricDomainCode;
		this.questions = questions;
	}

}
