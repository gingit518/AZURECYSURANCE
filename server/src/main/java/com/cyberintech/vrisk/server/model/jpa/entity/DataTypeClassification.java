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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Data Type Classifications Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2018-12-26
 */
@Entity
@Table(name = "data_type_classifications")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class DataTypeClassification
	implements IEntityWithMetadata, IMetadataAware<DataTypeClassificationMetadata> {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id", insertable = false, updatable = false)
	private Organizations organization;

	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "description")
	private String description;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "quant_metrics_to_data_type_classifications",
		joinColumns = {@JoinColumn(name = "data_type_id")},
		inverseJoinColumns = {@JoinColumn(name = "quant_metric_id")}
	)
	private Set<QuantMetrics> quantMetrics = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "regulations_to_data_type_classifications",
		joinColumns = {@JoinColumn(name = "data_type_classification_id")},
		inverseJoinColumns = {@JoinColumn(name = "regulation_id")}
	)
	private Set<Regulations> regulations = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "data_field_to_data_type_classification",
		joinColumns = {@JoinColumn(name = "data_type_classification_id")},
		inverseJoinColumns = {@JoinColumn(name = "data_field_id")}
	)
	private Set<DataFields> dataFields = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "data_domain_to_data_type_classification",
		joinColumns = {@JoinColumn(name = "data_type_classification_id")},
		inverseJoinColumns = {@JoinColumn(name = "data_domain_id")}
	)
	private Set<DataDomains> dataDomains = new HashSet<>();

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

	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "data_type_classifications_to_field_classifiers",
		joinColumns = {@JoinColumn(name = "data_type_classification_id")},
		inverseJoinColumns = {@JoinColumn(name = "field_classifier_id")}
	)
	private Set<FieldClassifiers> fieldClassifiers = new HashSet<>();

	@OneToMany(mappedBy = "dataTypeClassification", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<DataTypeClassificationMetadata> metadata = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id")
	private Users owner;

}
