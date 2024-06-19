package com.cyberintech.vrisk.server.model.dto.organization;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.Industries;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Organization Demo Data Config
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-07-08
 */
@Data
@ToString(of = {"organizationId", "riskModelId"})
@EqualsAndHashCode
public class OrganizationDemoDataConfigDTO {

	@Schema
	private Long organizationId;

	@Schema
	private Long riskModelId;

	@Schema
	private Boolean loadDemoBusinessUnits;

	@Schema
	private Boolean loadDemoSubsidiaries;

	@Schema
	private Boolean loadDemoSystems;

	@Schema
	private Boolean loadDemoTechnologies;

	@Schema
	private Boolean loadDemoQuantMetrics;

	@Schema
	private Boolean loadDemoUsers;

	@Schema
	private Boolean loadDemoVendors;

	@Schema
	private Boolean loadDemoGDPRArticles;

	@Schema
	private Boolean loadDemoQualitativeQuestionsCore;

	@Schema
	private Boolean loadDemoQualitativeQuestionsGDPR;

	@Schema
	private Boolean loadDemoQualitativeQuestionsVendorInternal;

	@Schema
	private Boolean loadDemoCyberSecurityMaturity;

	@Schema
	private Boolean loadDemoScoringQuestionsAmplified;

	@Schema
	private Boolean loadDemoScoringQuestionsConfidentiality;

	@Schema
	private Boolean loadDemoScoringQuestionsFFIECInherent;

	@Schema
	private Boolean loadDemoScoringQuestionsFFIECOrgMaturity;

	@Schema
	private Boolean loadDemoScoringQuestionsImpactSystem;

	@Schema
	private Boolean loadDemoScoringQuestionsImpactVendor;

	@Schema
	private Boolean loadDemoScoringQuestionsImpactGDPR;

	@Schema
	private Boolean loadDemoScoringQuestionsIntegrity;

	@Schema
	private Boolean loadDemoScoringQuestionsLikelihoodSystem;

	@Schema
	private Boolean loadDemoScoringQuestionsLikelihoodVendor;

	@Schema
	private Boolean loadDemoScoringQuestionsLikelihoodGDPR;

	@Schema
	private Boolean loadDemoScoringQuestionsMaturity;

	private Boolean loadDemoFramework_NIST_SP800;
	private Boolean loadDemoFramework_NIST_CSF;
	private Boolean loadDemoFramework_PCI_DSS;
	private Boolean loadDemoFramework_ISO_IEC_27001_2005;
	private Boolean loadDemoFramework_ISO_IEC_27001_2013;
	private Boolean loadDemoSecurityRequirements;

	/**
	 * Default constructor
	 */
	public OrganizationDemoDataConfigDTO(){
		;
	}
}
