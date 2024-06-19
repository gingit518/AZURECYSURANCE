package com.cyberintech.vrisk.server.model.dto.risk_model_domains;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.risk_domains.RiskDomainViewDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskModelDomains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;

/**
 * Risk Model Domain View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-08
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "riskDomainView"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class RiskModelDomainViewDTO extends DTOBase<RiskModelDomains> {

	@Schema
	private Long id;

	@Schema
	private RiskDomainViewDTO riskDomainView;

	@Schema
	private UserRefDTO createdByUser;

	@Schema
	private UserRefDTO riskManagementOwnerUser;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private Date createdAt;

	@Schema
	private Date updatedAt;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public RiskModelDomainViewDTO(RiskModelDomains entity) {
		super(entity);
	}

	@Override
	public void fromEntity(RiskModelDomains riskModelDomains) {
//		super.fromEntity(riskModelDomains);

		this.id = riskModelDomains.getId();
		this.createdAt = riskModelDomains.getCreatedAt();
		this.updatedAt = riskModelDomains.getUpdatedAt();
		this.name = riskModelDomains.getName();
		this.description = riskModelDomains.getDescription();

		// Trying to set Risk Domain
		if (riskModelDomains.getRiskDomain() != null) {
			setRiskDomainView(new RiskDomainViewDTO(riskModelDomains.getRiskDomain()));
		}

		// Trying to set Created User
		if (riskModelDomains.getCreatedBy() != null) {
			setCreatedByUser(new UserRefDTO(riskModelDomains.getCreatedBy()));
		}

		// Trying to set Risk Management Owner
		if (riskModelDomains.getRiskManagementOwner() != null) {
			setRiskManagementOwnerUser(new UserRefDTO(riskModelDomains.getRiskManagementOwner()));
		}
	}
}
