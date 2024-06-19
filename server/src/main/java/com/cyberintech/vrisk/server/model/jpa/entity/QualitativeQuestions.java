package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorTypeConverter;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Qualification Question Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-29
 */
@Entity
@Table(name = "qualitative_questions")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "question"})
@EqualsAndHashCode(of = {"id", "question"})
public class QualitativeQuestions implements IEntityWithMetadata {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "risk_model_id")
	private Long riskModelId;

	@Column(name = "code")
	private String code;

	@Column(name = "question")
	private String question;

	@Column(name = "description")
	private String description;

	@Column(name = "category_name")
	private String categoryName;

	// @Enumerated(EnumType.STRING)
	@Column(name = "vendor_type")
	@Convert(converter = VendorTypeConverter.class)
	private VendorType vendorType;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "qualification_metric_id")
	private QualMetrics qualitativeMetric;

	@Column(name = "ordinal")
	private Long ordinal;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by_id")
	private Users createdBy;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "updated_by_id")
	private Users updatedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at")
	private Date updatedAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_weight_id")
	private QuestionWeights questionWeight;

	@OneToMany(cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
	@JoinColumn(name = "question_id")
	private Set<QualitativeQuestionAnswers> answers = new HashSet<>();

	@OneToMany(cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
	@JoinColumn(name = "original_question_id")
	private Set<QuestionBranchingLogic> branchingLogic = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "questions_to_risk_types",
		joinColumns = {@JoinColumn(name = "question_id")},
		inverseJoinColumns = {@JoinColumn(name = "risk_type_id")}
	)
	private Set<RiskTypes> riskTypes = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "questions_to_vendors",
		joinColumns = {@JoinColumn(name = "question_id")},
		inverseJoinColumns = {@JoinColumn(name = "vendor_id")}
	)
	private Set<Organizations> vendors = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "questions_to_systems",
		joinColumns = {@JoinColumn(name = "question_id")},
		inverseJoinColumns = {@JoinColumn(name = "system_id")}
	)
	private Set<Systems> systems = new HashSet<>();

	@Column(name = "all_vendors_selected")
	private Boolean allVendorsSelected;

	@Column(name = "allow_multiple_answers")
	private Boolean allowMultipleAnswers;

	@Column(name = "allow_upload_as_answer")
	private Boolean allowUploadAsAnswer;

	@Column(name = "allow_text_as_answer")
	private Boolean allowTextAsAnswer;

	@Column(name = "allow_comment_to_answer")
	private Boolean allowCommentToAnswer;

	@Column(name = "is_technology_vendor")
	private Boolean isTechnologyVendor;

	@Column(name = "is_system_vendor")
	private Boolean isSystemVendor;

	@Column(name = "is_service_vendor")
	private Boolean isServiceVendor;

	@Column(name = "use_color_coding")
	private Boolean useColorCoding;

//	@Column(name = "is_internal")
//	private Boolean isInternal;

	@Column(name = "vendor_id")
	private Long vendorId;

}
