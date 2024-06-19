package com.cyberintech.vrisk.server.model.dto.systems;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemStatus;
import com.cyberintech.vrisk.server.model.jpa.entity.SystemToolRiskReductions;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Systems Tool Risk Reductions Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-26
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class SystemToolRiskReductionDTO extends DTOWithMetaData<SystemToolRiskReductions> {

	@Schema
	private Long id;

	@Schema
	private SystemRefDTO system;

	@Schema
	private Double riskReductionPercent;

	@Schema
	private Double riskReduction;

	@Schema
	private Double toolPrice;

	@Schema
	private String toolName;


	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public SystemToolRiskReductionDTO(SystemToolRiskReductions entity) {
		super(entity);
	}

	@Override
	public void fromEntity(SystemToolRiskReductions entity) {
		// super.fromEntity(entity);
		id = entity.getId();
		riskReductionPercent = entity.getRiskReductionPercent();
		riskReduction = entity.getRiskReduction();
		toolPrice = entity.getToolPrice();
		toolName = entity.getToolName();

		if (entity.getSystem() != null) {
			system = new SystemRefDTO(entity.getSystem());
		}
	}
}
