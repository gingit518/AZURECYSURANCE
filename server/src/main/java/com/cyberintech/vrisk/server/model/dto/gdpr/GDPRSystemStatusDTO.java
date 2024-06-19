package com.cyberintech.vrisk.server.model.dto.gdpr;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRSystemStatus;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * GDPR Article Items DTO Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-24
 */
@Setter
@Getter
@NoArgsConstructor
public class GDPRSystemStatusDTO extends DTOBase<GDPRSystemStatus> {

	@Schema
	private Long id;

	@Schema
	private SystemRefDTO system;

	@Schema
	private BusinessUnitRefDTO businessUnit;

	@Schema
	private Double compliance;

	@Schema
	private Double complianceMetric;

	@Schema
	private Double filesNumber;

	@Schema
	private Double articlesProcessed;

	@Schema
	private Double articlesNumber;

	@Schema
	private String comments;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public GDPRSystemStatusDTO(GDPRSystemStatus entity) {
		super(entity);
	}

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public GDPRSystemStatusDTO(GDPRSystemStatus entity, Systems system) {
		super(entity);

		// Set Entity
		if (entity == null) {
			if (system != null) {
				this.system = new SystemRefDTO(system);

				if (system.getBusinessUnit() != null) this.businessUnit = new BusinessUnitRefDTO(system.getBusinessUnit());
			}
		}
	}

	@Override
	public void fromEntity(GDPRSystemStatus entity) {
//		super.fromEntity(entity);

		id = entity.getId();
//		businessUnit = entity.getbusinessUnit();
		compliance = entity.getCompliance();
		complianceMetric = entity.getComplianceMetric();
		filesNumber = entity.getFilesNumber();
		articlesProcessed = entity.getArticlesProcessed();
		articlesNumber = entity.getArticlesNumber();
		comments = entity.getComments();

		if (entity.getSystem() != null) system = new SystemRefDTO(entity.getSystem());
	}
}
