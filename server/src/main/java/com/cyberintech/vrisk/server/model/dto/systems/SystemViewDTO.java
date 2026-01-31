package com.cyberintech.vrisk.server.model.dto.systems;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.DeploymentType;
import com.cyberintech.vrisk.server.model.jpa.domains.SLCT;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemStatus;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemType;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Systems View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-26
 */
@SuppressWarnings("serial")
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class SystemViewDTO extends DTOWithMetaData<Systems> {

	@Schema
	private Long id;

	@Schema
	@NotBlank(message = SLCT.VALIDATION$NAME_REQUIRED)
	private String name;

	@Schema
	// @NotBlank(message = SLCT.VALIDATION$DESCRIPTION_REQUIRED)
	private String description;

	@Schema
	private String versionNumber;

	@Schema
	// @NotNull(message = SLCT.VALIDATION$SYSTEM_STATUS_REQUIRED)
	private SystemStatus systemStatus;

	@Schema
	// @NotNull(message = SLCT.VALIDATION$DEPLOYMENT_TYPE_REQUIRED)
	private DeploymentType deploymentType;

	@Schema
	// @NotNull(message = SLCT.VALIDATION$SYSTEM_TYPE_REQUIRED)
	private SystemType systemType;

	@Schema
	// @NotNull(message = SLCT.VALIDATION$SYSTEM_OWNER_REQUIRED)
	private UserRefDTO owner;

	@Schema
	private UserRefDTO infosecFocalPerson;

	@Schema
	// @NotNull(message = SLCT.VALIDATION$SYSTEM_BUSINESS_UNIT_REQUIRED)
	private BusinessUnitRefDTO businessUnit;

	@Schema
	private Double numberOfRecProcessed;

	@Schema
	private Double rto;

	@Schema
	private Double rpo;

	@Schema
	private DataAssetClassificationRefDTO dataAssetClassification;

	@Schema
	private Double costToRestore;

	@Schema
	private List<SystemGeoParametersDTO> geoRecordsProcessed;

	@Schema
	private Set<SystemMetadataRefDTO> entityMetadata;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public SystemViewDTO(Systems entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Systems entity) {
//		super.fromEntity(entity);

		this.id = entity.getId();
		this.name = entity.getName();
		this.description = entity.getDescription();
		this.versionNumber = entity.getVersionNumber();
		this.systemStatus = entity.getSystemStatus();
		this.deploymentType = entity.getDeploymentType();
		this.systemType = entity.getSystemType();
		this.numberOfRecProcessed = entity.getNumberOfRecProcessed();
		this.rto = entity.getRto();
		this.rpo = entity.getRpo();
		this.costToRestore = entity.getCostToRestore();

		if (entity.getOwner() != null) {
			owner = new UserRefDTO(entity.getOwner());
		}

		if (entity.getInfosecFocalPerson() != null) {
			infosecFocalPerson = new UserRefDTO(entity.getInfosecFocalPerson());
		}

		if (entity.getBusinessUnit() != null) {
			businessUnit = new BusinessUnitRefDTO(entity.getBusinessUnit());
		}

		if (entity.getDataAssetClassification() != null) {
			dataAssetClassification = new DataAssetClassificationRefDTO(entity.getDataAssetClassification());
		}

		geoRecordsProcessed = Optional.ofNullable(entity.getSystemGeoParameters()).orElse(new HashSet<>()).stream().map(SystemGeoParametersDTO::new).collect(Collectors.toList());

		entityMetadata = Optional.ofNullable(entity.getMetadata())
			.stream()
			.flatMap(Set::stream).map(SystemMetadataRefDTO::new).collect(Collectors.toSet());
	}
}
