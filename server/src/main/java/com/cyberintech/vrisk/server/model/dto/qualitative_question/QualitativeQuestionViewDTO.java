package com.cyberintech.vrisk.server.model.dto.qualitative_question;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.AnswerViewDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.QualMetricsViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.QualitativeQuestions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Qualitative Question View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-06
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "question"})
@EqualsAndHashCode(of = {"id", "question"}, callSuper = false)
public class QualitativeQuestionViewDTO extends DTOWithMetaData<QualitativeQuestions> {

	@Schema
	private Long id;

	@Schema
	private String code;

	@Schema
	private String question;

	@Schema
	private String description;

	@Schema
	private VendorType vendorType;

	@Schema
	private String scoringTypeLabel;

	@Schema
	private Long ordinal;

	@Schema
	private QualMetricsViewDTO qualitativeMetric;

	@Schema
	private Boolean allVendorsSelected;

	@Schema
	private List<AnswerViewDTO> answers;

	@Schema
	private Boolean allowUploadAsAnswer;

	@Schema
	private Boolean allowTextAsAnswer;

	@Schema
	private Boolean allowCommentToAnswer;

	@Schema
	private Boolean isTechnologyVendor;

	@Schema
	private Boolean isSystemVendor;

	@Schema
	private Boolean isServiceVendor;

	@Schema
	private Boolean useColorCoding;

//	@Schema
//	private Boolean isInternal;

	@Schema
	private Boolean readOnly = false;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public QualitativeQuestionViewDTO(QualitativeQuestions entity) {
		super(entity);
	}

	@Override
	public void fromEntity(QualitativeQuestions entity) {
//		super.fromEntity(entity);

		this.id = entity.getId();
		this.code = entity.getCode();
		this.question = entity.getQuestion();
		this.description = entity.getDescription();
		this.vendorType = entity.getVendorType();
		this.scoringTypeLabel = VendorType.buildLabel(entity.getVendorType());
		this.ordinal = entity.getOrdinal();
		this.allVendorsSelected = entity.getAllVendorsSelected();
		this.allowUploadAsAnswer = entity.getAllowUploadAsAnswer();
		this.allowTextAsAnswer = entity.getAllowTextAsAnswer();
		this.allowCommentToAnswer = entity.getAllowCommentToAnswer();
		this.isTechnologyVendor = entity.getIsTechnologyVendor();
		this.isSystemVendor = entity.getIsSystemVendor();
		this.isServiceVendor = entity.getIsServiceVendor();
		this.useColorCoding = entity.getUseColorCoding();
//		this.isInternal = entity.getIsInternal();

		if (entity.getQualitativeMetric() != null) {
			qualitativeMetric = new QualMetricsViewDTO(entity.getQualitativeMetric());
		}

		answers = Optional.ofNullable(entity.getAnswers()).orElse(new HashSet<>()).stream().map(AnswerViewDTO::new).collect(Collectors.toList());
	}
}
