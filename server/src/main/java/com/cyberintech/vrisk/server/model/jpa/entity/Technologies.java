package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.entity.common.IMetadataAware;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Technology Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2018-12-27
 */
@Entity
@Table(name = "technologies")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
public class Technologies implements IEntityWithMetadata, IMetadataAware<TechnologiesMetadata> {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "notes")
	private String notes;

	@Column(name = "version")
	private String version;

	@Column(name = "risk_reduction")
	private Double riskReduction;

	@Column(name = "risk_reduction_percent")
	private Double riskReductionPercent;

	@Column(name = "tool_price")
	private Double toolPrice;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "technology_category_id")
	private TechnologyCategories technologyCategory;

	@Column(name = "subcategory_id", nullable = true)
	private Long technologySubcategoryId;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "subcategory_id", insertable = false, updatable = false)
	private TechnologySubcategories technologySubcategory;

	@Column(name = "class_type_id", nullable = true)
	private Long technologyClassTypeId;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "class_type_id", insertable = false, updatable = false)
	private TechnologyClassTypes technologyClassType;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "vendor_id")
	private Organizations vendor;

	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "technology_to_system",
		joinColumns = {@JoinColumn(name = "technology_id")},
		inverseJoinColumns = {@JoinColumn(name = "system_id")}
	)
	private Set<Systems> systems = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "data_domain_to_technology",
		joinColumns = {@JoinColumn(name = "technology_id")},
		inverseJoinColumns = {@JoinColumn(name = "data_domain_id")}
	)
	private Set<DataDomains> dataDomains = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "data_asset_classification_id")
	private DataAssetClassification assetClassification;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "country_id")
	private Country country;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "environment_type_id")
	private EnvironmentTypes environmentType;

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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "data_it_owner_id")
	private Users itOwner;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "data_business_owner_id")
	private Users businessOwner;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "state_id")
	private State state;

	@OneToMany(mappedBy = "technology", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<TechnologiesMetadata> metadata = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "infosec_focal_person_id")
	private Users infosecFocalPerson;

	@Column(name = "organization_owner_id")
	private Long organizationOwnerId;

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

	@Transient
	public String factorizeSystemName() {
		return getName() + getDescription();
	}

}
