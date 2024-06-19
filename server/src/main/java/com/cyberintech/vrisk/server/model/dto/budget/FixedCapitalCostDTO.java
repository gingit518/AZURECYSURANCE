package com.cyberintech.vrisk.server.model.dto.budget;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.FixedCapitalCosts;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;

/**
 * Variable Costs View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-03
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "system", "costType"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class FixedCapitalCostDTO extends DTOBase<FixedCapitalCosts> {

	@Schema
	private Long id;

	@Schema
	private OrganizationRefDTO vendor;

	@Schema
	private BusinessUnitRefDTO businessUnit;

	@Schema
	private LicenseTypeDTO licenseType;

	@Schema
	private Double licenseUsers;

	@Schema
	private Double licenseCpus;

	@Schema
	private Date startDate;

	@Schema
	private Date endDate;

	@Schema
	private Double licenseCost;

	@Schema
	private Double percentOfBudget;

	@Schema
	private Double totalCosts;

	@Schema
	private String securityToolName;

	@Schema
	private CybersecurityToolDTO cybersecurityTool;

	@Schema
	private TechnologyViewDTO technology;

	@Schema
	private Date costDate;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public FixedCapitalCostDTO(FixedCapitalCosts entity) {
		super(entity);
	}

	@Override
	public void fromEntity(FixedCapitalCosts entity) {
		// super.fromEntity(entity);
		id = entity.getId();
		licenseUsers = entity.getLicenseUsers();
		licenseCpus = entity.getLicenseCpus();
		licenseCost = entity.getLicenseCost();
		percentOfBudget = entity.getPercentOfBudget();
		totalCosts = entity.getTotalCosts();
		startDate = entity.getStartDate();
		endDate = entity.getEndDate();
		costDate = entity.getCostDate();

		if (entity.getVendor() != null) {
			vendor = new OrganizationRefDTO(entity.getVendor());
		}

		if (entity.getBusinessUnit() != null) {
			businessUnit = new BusinessUnitRefDTO(entity.getBusinessUnit());
		}

		if (entity.getLicenseType() != null) {
			licenseType = new LicenseTypeDTO(entity.getLicenseType());
		}
/*
		if (entity.getCybersecurityTool() != null) {
			cybersecurityTool = new CybersecurityToolDTO(entity.getCybersecurityTool());
		}
*/
		if (entity.getTechnology() != null) {
			technology = new TechnologyViewDTO(entity.getTechnology());
		}
	}

}
