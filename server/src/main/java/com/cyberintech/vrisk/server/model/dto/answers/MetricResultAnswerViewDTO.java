package com.cyberintech.vrisk.server.model.dto.answers;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationViewDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.AnswerViewDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.QuestionWeightDTO;
import com.cyberintech.vrisk.server.model.dto.qualitative_question.QualitativeQuestionRefDTO;
import com.cyberintech.vrisk.server.model.dto.qualitative_question.QualitativeQuestionViewDTO;
import com.cyberintech.vrisk.server.model.dto.risk_type.RiskTypeRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.MetricResultAnswers;
import com.cyberintech.vrisk.server.repository.results.QualitativeQuestionAnswerResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Metric Risk Answer View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-26
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "question"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class MetricResultAnswerViewDTO extends DTOWithMetaData<MetricResultAnswers> {

	@Schema
	private Long id;

	@Schema
	private QualitativeQuestionViewDTO question;

	@Schema
	private AnswerViewDTO answer;

	@Schema
	private List<AnswerViewDTO> answers;

	@Schema
	private Double multiplier;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public MetricResultAnswerViewDTO(MetricResultAnswers entity) {
		super(entity);
	}

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public MetricResultAnswerViewDTO(QualitativeQuestionAnswerResult entity) {
		fromEntity(entity);
	}

	/**
	 * Convert Entity to DTO
	 *
	 * @param entity
	 */
	public void fromEntity(QualitativeQuestionAnswerResult entity) {
		if (entity.getMetricResultAnswers() != null) {
			this.fromEntity(entity.getMetricResultAnswers());
		} else {
			// Trying to set Qualitative Question
			if (entity.getQuestion() != null) {
				setQuestion(new QualitativeQuestionViewDTO(entity.getQuestion()));
			}

			// Trying to set Question Weight
			if (entity.getAnswer() != null) {
				setAnswer(new AnswerViewDTO(entity.getAnswer()));
			}

			answers = Optional.ofNullable(entity.getQuestion().getAnswers()).orElse(new HashSet<>()).stream().map(tmpAnswer -> new AnswerViewDTO(tmpAnswer)).collect(Collectors.toList());
		}
	}

	@Override
	public void fromEntity(MetricResultAnswers entity) {
		this.id = entity.getId();
		this.multiplier = entity.getMultiplier();

		// Trying to set Qualitative Question
		if (entity.getQuestion() != null) {
			setQuestion(new QualitativeQuestionViewDTO(entity.getQuestion()));
		}

		// Trying to set Question Weight
		if (entity.getAnswer() != null) {
			setAnswer(new AnswerViewDTO(entity.getAnswer()));
		}

		answers = Optional.ofNullable(entity.getQuestion().getAnswers()).orElse(new HashSet<>()).stream().map(tmpAnswer -> new AnswerViewDTO(tmpAnswer)).collect(Collectors.toList());
	}
}
