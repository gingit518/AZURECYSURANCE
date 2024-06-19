package com.cyberintech.vrisk.server.model.dto.gdpr;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPROrganizationStatus;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRSystemStatus;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GDPR Organization Status Items DTO Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-11-12
 */
@Setter
@Getter
@NoArgsConstructor
public class GDPROrganizationStatusDTO extends DTOBase<GDPROrganizationStatus> {

	@Schema
	private Long id;

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
	public GDPROrganizationStatusDTO(GDPROrganizationStatus entity) {
		super(entity);
	}

	@Override
	public void fromEntity(GDPROrganizationStatus entity) {
		// super.fromEntity(entity);
		id = entity.getId();
		compliance = entity.getCompliance();
		complianceMetric = entity.getComplianceMetric();
		filesNumber = entity.getFilesNumber();
		articlesProcessed = entity.getArticlesProcessed();
		articlesNumber = entity.getArticlesNumber();
		comments = entity.getComments();
	}
}
