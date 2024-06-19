package com.cyberintech.vrisk.server.model.dto.qual_metrics;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.AnswerWeight;
import com.cyberintech.vrisk.server.model.jpa.entity.QualitativeQuestionAnswers;
import com.cyberintech.vrisk.server.model.jpa.entity.QuestionAnswersForSystem;
import com.cyberintech.vrisk.server.model.jpa.entity.QuestionAnswersForVendor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

/**
 * Qualification Metrics Answer View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-03
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "answer"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class AnswerViewDTO extends DTOBase<QualitativeQuestionAnswers> {

	@Schema
	private Long id;

	@Schema
	private String answer;

	@Schema
	private AnswerWeightDTO answerWeight;

	@Schema
	private DocumentDTO document;

	@Schema
	private String answerText;

	@Schema
	private String answerComment;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public AnswerViewDTO(QualitativeQuestionAnswers entity) {
		super(entity);
	}

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public AnswerViewDTO(QuestionAnswersForSystem entity) {
		super(entity.getAnswer());

		if (entity.getDocument() != null) {
			document = new DocumentDTO(entity.getDocument());
		}
		if (StringUtils.isNotEmpty(entity.getAnswerText())) {
			answerText = entity.getAnswerText();
		}
		if (StringUtils.isNotEmpty(entity.getAnswerComment())) {
			answerComment = entity.getAnswerComment();
		}
	}

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public AnswerViewDTO(QuestionAnswersForVendor entity) {
		super(entity.getAnswer());

		if (entity.getDocument() != null) {
			document = new DocumentDTO(entity.getDocument());
		}
		if (StringUtils.isNotEmpty(entity.getAnswerText())) {
			answerText = entity.getAnswerText();
		}
		if (StringUtils.isNotEmpty(entity.getAnswerComment())) {
			answerComment = entity.getAnswerComment();
		}
	}

	@Override
	public void fromEntity(QualitativeQuestionAnswers entity) {
//		super.fromEntity(entity);

		this.id = entity.getId();
		this.answer = entity.getAnswer();
//		this.document = entity.getDocument();
//		this.answerText = entity.getAnswerText();

		if (entity.getAnswerWeight() != null) {
			answerWeight = new AnswerWeightDTO(entity.getAnswerWeight());
		}
	}
}
