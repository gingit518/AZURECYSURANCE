package com.cyberintech.vrisk.server.model.dto.qual_metrics;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.QualMetrics;
import com.cyberintech.vrisk.server.model.jpa.entity.QualitativeQuestionAnswers;
import com.cyberintech.vrisk.server.model.jpa.entity.QualitativeQuestions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Set;

/**
 * Question View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-03
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "question"})
@EqualsAndHashCode(of = {"id", "question"}, callSuper = false)
public class QualificationQuestionViewDTO extends DTOWithMetaData<QualitativeQuestions> {

	@Schema
	private Long id;

	@Schema
	private Long riskModelId;

	@Schema
	private String question;

	@Schema
	private String description;

	@Schema
	private Long ordinal;

	@Schema
	private VendorType vendorType;

	@Schema
	private ItemViewDTO<QualMetrics> qualitativeMetric;

	@Schema
	private Set<ItemViewDTO<QualitativeQuestionAnswers>> answers;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public QualificationQuestionViewDTO(QualitativeQuestions entity) {
		super(entity);
	}

	@Override
	public void fromEntity(QualitativeQuestions entity) {
		// super.fromEntity(entity);
		id = entity.getId();
		riskModelId = entity.getRiskModelId();
		question = entity.getQuestion();
		description = entity.getDescription();
		ordinal = entity.getOrdinal();
		vendorType = entity.getVendorType();
	}
}
