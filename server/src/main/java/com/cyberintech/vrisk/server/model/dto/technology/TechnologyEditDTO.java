package com.cyberintech.vrisk.server.model.dto.technology;

import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.datadomains.DataDomainsDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyClassTypeDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologySubcategoryDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.EnvironmentTypes;
import com.cyberintech.vrisk.server.model.jpa.entity.Technologies;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Technology Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@Setter
@Getter
@NoArgsConstructor
public class TechnologyEditDTO extends TechnologyViewDTO {

//	@Schema
//	private Long organizationId;

	@Schema
	private List<SystemRefDTO> systems;

	@Schema
	private List<DataDomainsDTO> dataDomains;

	@Schema
	private DataAssetClassificationRefDTO assetClassification;

	@Schema
	private EnvironmentTypesDTO environmentType;

	private String osName;

	private String serialNumber;

	private Date eolDate;

	private Date warrantyExpiration;

	private String assetName;

	private String ipAddress;

	private String assetDomainFunction;

	private String discoverySource;

	private String deviceId;

	private String hardwareSubstatus;

	private String ownerType;

	private String location;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public TechnologyEditDTO(Technologies entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Technologies entity) {
		super.fromEntity(entity);

		// Set Technology Flow properties
		osName = entity.getOsName();
		serialNumber = entity.getSerialNumber();
		eolDate = entity.getEolDate();
		warrantyExpiration = entity.getWarrantyExpiration();
		assetName = entity.getAssetName();
		ipAddress = entity.getIpAddress();
		assetDomainFunction = entity.getAssetDomainFunction();
		discoverySource = entity.getDiscoverySource();
		deviceId = entity.getDeviceId();
		hardwareSubstatus = entity.getHardwareSubstatus();
		ownerType = entity.getOwnerType();
		location = entity.getLocation();

		if (entity.getAssetClassification() != null) {
			assetClassification = new DataAssetClassificationRefDTO(entity.getAssetClassification());
		}

		systems = Optional.ofNullable(entity.getSystems()).orElse(new HashSet<>()).stream().map(SystemRefDTO::new).collect(Collectors.toList());
		dataDomains = Optional.ofNullable(entity.getDataDomains()).orElse(new HashSet<>()).stream().map(DataDomainsDTO::new).collect(Collectors.toList());

		if (entity.getEnvironmentType() != null) {
			environmentType = new EnvironmentTypesDTO(entity.getEnvironmentType());
		}
	}
}
