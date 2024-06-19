package com.cyberintech.vrisk.server.model.dto.policy;


import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentTypeViewDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.SecurityRequirementDTO;
import com.cyberintech.vrisk.server.model.dto.budget.CyberRoleDTO;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPRArticleItemDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.SLCT;
import com.cyberintech.vrisk.server.model.jpa.entity.CyberRoles;
import com.cyberintech.vrisk.server.model.jpa.entity.Policies;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Business Unit View Entity Definition
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2018-01-08
 */
@SuppressWarnings("serial")
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class PolicyViewDTO extends DTOBase<Policies> {

	@Schema
	private Long id;

	@Schema
	@NotBlank(message = SLCT.VALIDATION$NAME_REQUIRED)
	private String name;

	@Schema
	private String version;

	@Schema
	private String overview;

	@Schema
	private String purpose;

	@Schema
	private String scope;

	@Schema
	private String enforcement;

	@Schema
	private String exceptions;

	@Schema
	private String definitions;

	@Schema
	private Date createdAt;

	@Schema
	private UserRefDTO createdBy;

	@Schema
	private Date approvedAt;

	@Schema
	private UserRefDTO approvedBy;

	@Schema
	private Date annualReviewDate;

	@Schema
	private List<AssessmentTypeViewDTO> assessmentTypes;

	@Schema
	private List<GDPRArticleItemDTO> gdprArticles;

	@Schema
	private DocumentDTO document;

	@Schema
	private List<SecurityRequirementDTO> securityRequirements;

	@Schema
	private List<PolicyRefDTO> relatedPolicies;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public PolicyViewDTO(Policies entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Policies entity) {
	//	super.fromEntity(entity);

		id = entity.getId();
		name = entity.getName();
		version = entity.getVersion();
		createdAt = entity.getCreatedAt();
		approvedAt = entity.getApprovedAt();
		annualReviewDate = entity.getAnnualReviewDate();

		overview = entity.getOverview();
		purpose = entity.getPurpose();
		scope = entity.getScope();
		enforcement = entity.getEnforcement();
		exceptions = entity.getExceptions();
		definitions = entity.getDefinitions();

		if (entity.getCreatedBy() != null) {
			createdBy = new UserRefDTO(entity.getCreatedBy());
		}
		if (entity.getApprovedBy() != null) {
			approvedBy = new UserRefDTO(entity.getApprovedBy());
		}
		if (entity.getDocument() != null) {
			document = new DocumentDTO(entity.getDocument());
		}

		assessmentTypes = Optional.ofNullable(entity.getAssessmentTypes()).orElse(new HashSet<>()).stream().map(AssessmentTypeViewDTO::new).collect(Collectors.toList());
		gdprArticles = Optional.ofNullable(entity.getGdprArticles()).orElse(new HashSet<>()).stream().map(GDPRArticleItemDTO::new).collect(Collectors.toList());
		// securityRequirements = Optional.ofNullable(entity.getSecurityRequirements()).orElse(new HashSet<>()).stream().map(SecurityRequirementDTO::new).collect(Collectors.toList());
		relatedPolicies = Optional.ofNullable(entity.getRelatedPolicies()).orElse(new HashSet<>()).stream().map(PolicyRefDTO::new).collect(Collectors.toList());
	}
}
