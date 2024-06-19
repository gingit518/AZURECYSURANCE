package com.cyberintech.vrisk.server.model.dto.qualitative_question;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.AnswerViewDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.AnswerWeightDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.QuestionBranchingLogic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
public class QuestionBranchingLogicViewDTO extends DTOBase<QuestionBranchingLogic> {

	@Schema
	private Long id;

	@Schema
	private AnswerViewDTO answer;

	@Schema
	private QualitativeQuestionRefDTO question;

	@Schema
	private List<AnswerViewDTO> answerList;

	@Schema
	private Long ordinal;

	@Schema
	private Long operation;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public QuestionBranchingLogicViewDTO(QuestionBranchingLogic entity) {
		super(entity);
	}

	@Override
	public void fromEntity(QuestionBranchingLogic entity) {
//		super.fromEntity(entity);

		id = entity.getId();
		ordinal = entity.getOrdinal();
		operation = entity.getOperation();

		if (entity.getAnswer() != null) {
			answer = new AnswerViewDTO(entity.getAnswer());
		}

		if (entity.getQuestion() != null) {
			question = new QualitativeQuestionRefDTO(entity.getQuestion());

			if (entity.getQuestion().getAnswers() != null) {
				answerList = Optional.ofNullable(entity.getQuestion().getAnswers()).orElse(new HashSet<>()).stream().map(AnswerViewDTO::new).collect(Collectors.toList());
			}
		}
	}
}
