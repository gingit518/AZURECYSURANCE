package com.cyberintech.vrisk.server.model.dto.systems;

import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.datadomains.DataDomainsDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.process.ProcessRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyAssets;
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
 * Technology Asset Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2024-01-09
 */
@SuppressWarnings("serial")
@Setter
@Getter
@NoArgsConstructor
public class TechnologyAssetEditDTO extends TechnologyAssetViewDTO {

	@Schema
	private Boolean isMAAsset;

	@Schema
	private Boolean isEtl;

	/*
	@Schema
	private List<TechnologyRefDTO> technologies;
	*/

	private List<OrganizationRefDTO> associateVendors;

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


//	@Schema
//	private Long organizationId;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public TechnologyAssetEditDTO(TechnologyAssets entity) {
		super(entity);
	}

	@Override
	public void fromEntity(TechnologyAssets entity) {
		super.fromEntity(entity);

		isEtl = entity.getIsEtl();
		isMAAsset = entity.getIsMAAsset();

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

		// technologies = Optional.ofNullable(entity.getTechnologies()).orElse(new HashSet<>()).stream().map(TechnologyRefDTO::of).collect(Collectors.toList());
	}
}
