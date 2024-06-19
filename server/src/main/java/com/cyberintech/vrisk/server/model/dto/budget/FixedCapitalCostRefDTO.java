package com.cyberintech.vrisk.server.model.dto.budget;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
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
@ToString(of = {"id", "cyberSecurityToolName"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class FixedCapitalCostRefDTO extends DTOBase<FixedCapitalCosts> {

	@Schema
	private Long id;

	@Schema
	private OrganizationRefDTO vendor;

	@Schema
	private Double totalCosts;

	@Schema
	@Deprecated
	private String securityToolName;

	@Schema
	private String cyberSecurityToolName;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public FixedCapitalCostRefDTO(FixedCapitalCosts entity) {
		super(entity);
	}

	@Override
	public void fromEntity(FixedCapitalCosts entity) {
		// super.fromEntity(entity);
		id = entity.getId();
		totalCosts = entity.getTotalCosts();

		if (entity.getVendor() != null) {
			vendor = new OrganizationRefDTO(entity.getVendor());
		}

		if (entity.getCybersecurityTool() != null) {
			cyberSecurityToolName = entity.getCybersecurityTool().getName();
		}
	}

}
