package com.cyberintech.vrisk.server.model.dto.qualitative_question;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationViewDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.AnswerViewDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.AnswerWeightDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.QualMetricsViewDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.QuestionWeightDTO;
import com.cyberintech.vrisk.server.model.dto.risk_type.RiskTypeViewDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.QualitativeQuestions;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Qualitative Question View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-11
 */
@Setter
@Getter
@ToString(of = {"id", "question"})
@EqualsAndHashCode(of = {"id", "question"}, callSuper = false)
public class QualitativeQuestionEditDTO extends DTOBase<QualitativeQuestions> {

	@Schema
	private Long id;

	@Schema
	private Long riskModelId;

	@Schema
	private String code;

	@Schema
	private String question;

	@Schema
	private String description;

	@Schema
	private String categoryName;

	@Schema
	private VendorType vendorType;

	@Schema
	private QualMetricsViewDTO qualitativeMetric;

	@Schema
	private Long ordinal;

	@Schema
	private List<AnswerViewDTO> answers;

	@Schema
	private List<AnswerWeightDTO> answerWeights;

	@Schema
	private List<QuestionBranchingLogicViewDTO> branchingLogic;

	@Schema
	private String branchingLogicString;

	@JsonIgnore
	@Schema
	private String gdprString;

	@Schema
	private QuestionWeightDTO questionWeight;

	@Schema
	private List<RiskTypeViewDTO> riskTypes;

	@Schema
	private List<OrganizationViewDTO> vendors;

	@Schema
	private List<SystemRefDTO> systems;

	@Schema
	private Boolean allVendorsSelected;

	@Schema
	private Boolean allowMultipleAnswers;

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
	 * Default constructor
	 */
	public QualitativeQuestionEditDTO() {
		super();

		answers = new ArrayList<>();
	}

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public QualitativeQuestionEditDTO(QualitativeQuestions entity) {
		super(entity);
	}

	@Override
	public void fromEntity(QualitativeQuestions entity) {
//		super.fromEntity(entity);

		this.id = entity.getId();
		this.riskModelId = entity.getRiskModelId();
		this.code = entity.getCode();
		this.question = entity.getQuestion();
		this.description = entity.getDescription();
		this.categoryName = entity.getCategoryName();
		this.vendorType = entity.getVendorType();
		this.ordinal = entity.getOrdinal();
//		this.answerWeights = entity.getAnswerWeights();
//		this.branchingLogicString = entity.getBranchingLogicString();
//		this.gdprString = entity.getGdprString();
		this.allVendorsSelected = entity.getAllVendorsSelected();
		this.allowUploadAsAnswer = entity.getAllowUploadAsAnswer();
		this.allowMultipleAnswers = entity.getAllowMultipleAnswers();
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

		if (entity.getQuestionWeight() != null) {
			questionWeight = new QuestionWeightDTO(entity.getQuestionWeight());
		}

		answers = Optional.ofNullable(entity.getAnswers()).orElse(new HashSet<>()).stream().map(AnswerViewDTO::new).collect(Collectors.toList());
		riskTypes = Optional.ofNullable(entity.getRiskTypes()).orElse(new HashSet<>()).stream().map(RiskTypeViewDTO::new).collect(Collectors.toList());
		vendors = Optional.ofNullable(entity.getVendors()).orElse(new HashSet<>()).stream().map(OrganizationViewDTO::new).collect(Collectors.toList());
		systems = Optional.ofNullable(entity.getSystems()).orElse(new HashSet<>()).stream().map(SystemRefDTO::new).collect(Collectors.toList());
		branchingLogic = Optional.ofNullable(entity.getBranchingLogic()).orElse(new HashSet<>()).stream().map(QuestionBranchingLogicViewDTO::new).collect(Collectors.toList());
	}
}
