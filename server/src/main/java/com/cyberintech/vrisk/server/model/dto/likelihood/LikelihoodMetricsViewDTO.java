package com.cyberintech.vrisk.server.model.dto.likelihood;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationViewDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.QuestionWeightDTO;
import com.cyberintech.vrisk.server.model.dto.qualitative_question.QualitativeQuestionRefDTO;
import com.cyberintech.vrisk.server.model.dto.risk_type.RiskTypeRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.LikelihoodMetrics;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Risk Model Domain View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-08
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "question"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class LikelihoodMetricsViewDTO extends DTOWithMetaData<LikelihoodMetrics> {

	@Schema
	private Long id;

	@Schema
	private QualitativeQuestionRefDTO question;

	@Schema
	private QuestionWeightDTO questionWeight;

	@Schema
	private RiskTypeRefDTO riskType;

	@Schema
	private OrganizationViewDTO vendor;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public LikelihoodMetricsViewDTO(LikelihoodMetrics entity) {
		super(entity);
	}

	@Override
	public void fromEntity(LikelihoodMetrics entity) {
		super.fromEntity(entity);

		// Trying to set Qualitative Question
		if (entity.getQuestion() != null) {
			setQuestion(new QualitativeQuestionRefDTO(entity.getQuestion()));
		}

		// Trying to set Question Weight
		if (entity.getQuestionWeight() != null) {
			setQuestionWeight(new QuestionWeightDTO(entity.getQuestionWeight()));
		}

		// Trying to set Risk Type
		if (entity.getRiskType() != null) {
			setRiskType(new RiskTypeRefDTO(entity.getRiskType()));
		}

		// Trying to set Vendor
		if (entity.getVendor() != null) {
			setVendor(new OrganizationViewDTO(entity.getVendor()));
		}

	}
}
