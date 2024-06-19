package com.cyberintech.vrisk.server.model.dto.audit.items;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.AnswerViewDTO;
import com.cyberintech.vrisk.server.model.dto.qualitative_question.QualitativeQuestionRefDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.QuestionAnswersForVendor;
import lombok.*;

/**
 * Qualitative Question View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-07-23
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "question"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class QuestionAnswerForVendorsDTO extends DTOBase<QuestionAnswersForVendor> {

	private Long id;
	private QualitativeQuestionRefDTO question;
	private AnswerViewDTO answer;
	private OrganizationRefDTO vendor;
	private DocumentDTO document;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public QuestionAnswerForVendorsDTO(QuestionAnswersForVendor entity) {
		super(entity);
	}

	@Override
	public void fromEntity(QuestionAnswersForVendor entity) {
//		super.fromEntity(entity);
		this.id = entity.getId();

		if (entity.getQuestion() != null) question = new QualitativeQuestionRefDTO(entity.getQuestion());
		if (entity.getAnswer() != null) answer = new AnswerViewDTO(entity.getAnswer());
		if (entity.getVendor() != null) vendor = new OrganizationRefDTO(entity.getVendor());
		if (entity.getDocument() != null) document = new DocumentDTO(entity.getDocument());
	}
}
