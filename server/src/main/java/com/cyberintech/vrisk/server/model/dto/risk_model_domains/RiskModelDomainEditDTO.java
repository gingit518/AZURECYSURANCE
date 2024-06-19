package com.cyberintech.vrisk.server.model.dto.risk_model_domains;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskModelDomains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Risk Model Domain Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-09
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "riskModelId", "riskDomainId"})
@EqualsAndHashCode(of = {"id", "riskModelId", "riskDomainId"}, callSuper = false)
public class RiskModelDomainEditDTO extends DTOBase<RiskModelDomains> {

	@Schema
	private Long id;

	@Schema
	private Long riskModelId;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private Long riskDomainId;

	@Schema
	private Long riskManagementOwnerUserId;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public RiskModelDomainEditDTO(RiskModelDomains entity) {
//		super(entity);

		id = entity.getId();
		riskModelId = entity.getRiskModelId();
		name = entity.getName();
		description = entity.getDescription();

		if (entity.getRiskDomain() != null) {
			riskDomainId = entity.getRiskDomain().getId();
		}
		if (entity.getRiskManagementOwner() != null) {
			riskManagementOwnerUserId = entity.getRiskManagementOwner().getId();
		}
	}

}
