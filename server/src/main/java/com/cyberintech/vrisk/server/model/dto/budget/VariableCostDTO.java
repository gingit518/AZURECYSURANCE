package com.cyberintech.vrisk.server.model.dto.budget;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.FixedCapitalCosts;
import com.cyberintech.vrisk.server.model.jpa.entity.FixedOperationalCosts;
import com.cyberintech.vrisk.server.model.jpa.entity.VariableCosts;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fixed Capital Costs View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-03
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class VariableCostDTO extends DTOBase<VariableCosts> {

	@Schema
	private Long id;

	@Schema
	private SystemRefDTO system;

	@Schema
	private CostTypeDTO costType;

	@Schema
	private Double equipmentCost;

	@Schema
	private Double personnelCost;

	@Schema
	private Double percentOfTime;

	@Schema
	private Double percentOfBudget;

	@Schema
	private Double totalCosts;

	@Schema
	private String comments;

	@Schema
	private Date costDate;

	private List<FixedCapitalCostRefDTO> fixedCapitalCosts;

	private List<FixedOperationalCostRefDTO> fixedOperationalCosts;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public VariableCostDTO(VariableCosts entity) {
		super(entity);
	}

	@Override
	public void fromEntity(VariableCosts entity) {
		id = entity.getId();
		equipmentCost = entity.getEquipmentCost();
		personnelCost = entity.getPersonnelCost();
		percentOfTime = entity.getPercentOfTime();
		percentOfBudget = entity.getPercentOfBudget();
		totalCosts = entity.getTotalCosts();
		comments = entity.getComments();

		if (entity.getSystem() != null) {
			system = new SystemRefDTO(entity.getSystem());
		}

		if (entity.getCostType() != null) {
			costType = new CostTypeDTO(entity.getCostType());
		}

		fixedCapitalCosts = entity.getFixedCapitalCosts().stream().map(FixedCapitalCostRefDTO::new).collect(Collectors.toList());
		fixedOperationalCosts = entity.getFixedOperationalCosts().stream().map(FixedOperationalCostRefDTO::new).collect(Collectors.toList());
	}
}
