package com.cyberintech.vrisk.server.model.dto.systems;

import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.datadomains.DataDomainsDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.process.ProcessRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.SLCT;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * System Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@SuppressWarnings("serial")
@Setter
@Getter
@NoArgsConstructor
public class SystemEditDTO extends SystemViewDTO {

	@Schema
	private Boolean isMAAsset;

	@Schema
	private Boolean isEtl;

	@Schema
	@NotEmpty(message = SLCT.VALIDATION$DATA_CLASSIFICATION_REQUIRED)
	private List<DataTypeClassificationRefDTO> dataTypeClassifications;

	@Schema
	private List<DataDomainsDTO> dataDomains;

	@Schema
	private List<TechnologyRefDTO> technologies;

	@Schema
	private List<ProcessRefDTO> processes;

	@Schema
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
	public SystemEditDTO(Systems entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Systems entity) {
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

		dataTypeClassifications = Optional.ofNullable(entity.getDataTypeClassifications()).orElse(new HashSet<>()).stream().map(DataTypeClassificationRefDTO::new).collect(Collectors.toList());
		dataDomains = Optional.ofNullable(entity.getDataDomains()).orElse(new HashSet<>()).stream().map(DataDomainsDTO::new).collect(Collectors.toList());
		technologies = Optional.ofNullable(entity.getTechnologies()).orElse(new HashSet<>()).stream().map(TechnologyRefDTO::of).collect(Collectors.toList());
		processes = Optional.ofNullable(entity.getProcesses()).orElse(new HashSet<>()).stream().map(ProcessRefDTO::new).collect(Collectors.toList());
	}
}
