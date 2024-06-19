package com.cyberintech.vrisk.server.model.dto.metric_risk;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationViewDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.QuestionWeightDTO;
import com.cyberintech.vrisk.server.model.dto.qualitative_question.QualitativeQuestionRefDTO;
import com.cyberintech.vrisk.server.model.dto.risk_type.RiskTypeRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.MetricDomain;
import com.cyberintech.vrisk.server.model.jpa.entity.MetricDomains;
import com.cyberintech.vrisk.server.model.jpa.entity.MetricRisks;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Metric Risk Domain View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-18
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "question"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class MetricRisksViewDTO extends DTOWithMetaData<MetricRisks> {

	@Schema
	private Long id;

	@Schema
	private ItemViewDTO<MetricDomains> metricDomain;

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
	public MetricRisksViewDTO(MetricRisks entity) {
		super(entity);
	}

	@Override
	public void fromEntity(MetricRisks entity) {
		super.fromEntity(entity);

		// Trying to set Metric Domain
		if (entity.getMetricDomain() != null) {
			setMetricDomain(new ItemViewDTO<>(entity.getMetricDomain()));
		}

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
