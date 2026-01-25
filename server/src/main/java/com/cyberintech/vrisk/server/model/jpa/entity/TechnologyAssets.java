package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.DeploymentType;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemStatus;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemType;
import com.cyberintech.vrisk.server.model.jpa.entity.common.IMetadataAware;
import com.cyberintech.vrisk.server.model.jpa.entity.converters.MapOfObjectsConverter;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Technology Asset Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2024-01-09
 */
@Entity
@Table(name = "technology_assets")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
public class TechnologyAssets implements IEntityWithMetadata {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id", insertable = false, updatable = false)
	private Organizations organization;

	@Enumerated(EnumType.STRING)
	@Column(name = "technology_asset_status")
	private SystemStatus systemStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "deployment_type")
	private DeploymentType deploymentType;

	@Enumerated(EnumType.STRING)
	@Column(name = "technology_asset_type")
	private SystemType systemType;

	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "version_number")
	private String versionNumber;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "owner_id")
	private Users owner;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "infosec_focal_person_id")
	private Users infosecFocalPerson;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "business_unit_id")
	private BusinessUnits businessUnit;

	@Column(name = "number_of_rec_processed")
	private Double numberOfRecProcessed;

	/**
	 * Recovery time objective (RTO)
	 */
	@Column(name = "rto")
	private Double rto;

	/**
	 * Recovery point objective (RPO)
	 */
	@Column(name = "rpo")
	private Double rpo;

	@Column(name = "is_ma_asset")
	private Boolean isMAAsset;

	@Column(name = "is_etl")
	private Boolean isEtl;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "technology_category_id")
	private TechnologyCategories technologyCategory;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "technology_subcategory_id")
	private TechnologySubcategories technologySubcategory;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "technology_class_type_id")
	private TechnologyClassTypes technologyClassType;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "technology_id")
	private Technologies technology;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "manufacturer_id")
	private Organizations manufacturer;

	/*
	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "technology_to_system",
		joinColumns = {@JoinColumn(name = "system_id")},
		inverseJoinColumns = {@JoinColumn(name = "technology_id")}
	)
	private Set<Technologies> technologies = new HashSet<>();
	*/

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "created_by_id")
	private Users createdBy;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "updated_by_id")
	private Users updatedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at")
	private Date updatedAt;

	@Column(name = "cost_to_restore")
	private Double costToRestore;

	@Column(name = "os_name")
	private String osName;

	@Column(name = "serial_number")
	private String serialNumber;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "eol_date")
	private Date eolDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "warranty_expiration")
	private Date warrantyExpiration;

	@Column(name = "asset_name")
	private String assetName;

	@Column(name = "ip_address")
	private String ipAddress;

	@Column(name = "ip_addresses")
	// @Convert(converter = MapOfObjectsConverter.class)
	private String ipAddresses;

	@Column(name = "asset_domain_function")
	private String assetDomainFunction;

	@Column(name = "discovery_source")
	private String discoverySource;

	@Column(name = "device_id")
	private String deviceId;

	@Column(name = "hardware_substatus")
	private String hardwareSubstatus;

	@Column(name = "owner_type")
	private String ownerType;

	@Column(name = "location")
	private String location;

}
