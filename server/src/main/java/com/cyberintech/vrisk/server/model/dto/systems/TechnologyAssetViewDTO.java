package com.cyberintech.vrisk.server.model.dto.systems;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyClassTypeRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologySubcategoryRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.DeploymentType;
import com.cyberintech.vrisk.server.model.jpa.domains.SLCT;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemStatus;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemType;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyAssets;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Technology Asset View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2024-01-09
 */
@SuppressWarnings("serial")
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class TechnologyAssetViewDTO extends DTOWithMetaData<TechnologyAssets> {

	@Schema
	private Long id;

	@Schema
	@NotBlank(message = SLCT.VALIDATION$NAME_REQUIRED)
	private String name;

	@Schema
	private String description;

	@Schema
	private String versionNumber;

	@Schema
	private SystemStatus systemStatus;

	@Schema
	private DeploymentType deploymentType;

	@Schema
	private SystemType systemType;

	@Schema
	private UserRefDTO owner;

	@Schema
	private UserRefDTO infosecFocalPerson;

	@Schema
	private BusinessUnitRefDTO businessUnit;

	@Schema
	private Double numberOfRecProcessed;

	@Schema
	private Double rto;

	@Schema
	private Double rpo;

	@Schema
	private TechnologyCategoryRefDTO technologyCategory;

	@Schema
	private TechnologySubcategoryRefDTO technologySubcategory;

	@Schema
	private TechnologyClassTypeRefDTO technologyClassType;

	@Schema
	private TechnologyRefDTO technology;

	@Schema
	private OrganizationRefDTO manufacturer;

	@Schema
	private Double costToRestore;

	private Date eolDate;

	private String assetName;

	private String ipAddress;

	private String deviceId;

	private String location;


	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public TechnologyAssetViewDTO(TechnologyAssets entity) {
		super(entity);
	}

	@Override
	public void fromEntity(TechnologyAssets entity) {
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

		eolDate = entity.getEolDate();
		assetName = entity.getAssetName();
		ipAddress = entity.getIpAddress();
		deviceId = entity.getDeviceId();
		location = entity.getLocation();

		if (entity.getOwner() != null) {
			owner = new UserRefDTO(entity.getOwner());
		}

		if (entity.getInfosecFocalPerson() != null) {
			infosecFocalPerson = new UserRefDTO(entity.getInfosecFocalPerson());
		}

		if (entity.getBusinessUnit() != null) {
			businessUnit = new BusinessUnitRefDTO(entity.getBusinessUnit());
		}

		if (entity.getTechnologyCategory() != null) {
			technologyCategory = new TechnologyCategoryRefDTO(entity.getTechnologyCategory());
		}

		if (entity.getTechnologySubcategory() != null) {
			technologySubcategory = new TechnologySubcategoryRefDTO(entity.getTechnologySubcategory());
		}

		if (entity.getTechnologyClassType() != null) {
			technologyClassType = new TechnologyClassTypeRefDTO(entity.getTechnologyClassType());
		}

		if (entity.getTechnology() != null) {
			technology = new TechnologyRefDTO(entity.getTechnology());
		}

		if (entity.getManufacturer() != null) {
			manufacturer = new OrganizationRefDTO(entity.getManufacturer());
		}

	}
}
