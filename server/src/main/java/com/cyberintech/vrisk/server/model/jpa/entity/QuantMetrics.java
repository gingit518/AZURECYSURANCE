package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.DeploymentType;
import com.cyberintech.vrisk.server.model.jpa.domains.QuantMetricLevel;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Quantification Metrics Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-19
 */
@Entity
@Table(name = "quant_metrics")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"})
public class QuantMetrics implements IEntityWithMetadata {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "name", unique = true, nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "risk_model_id")
	private Long riskModelId;

	@Column(name = "ordinal")
	private Long ordinal;

	@Column(name = "is_regulation_restricted")
	private Boolean isRegulationRestricted;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "quant_id")
	private Quants quant;

	@Enumerated(EnumType.STRING)
	@Column(name = "quant_metric_level")
	private QuantMetricLevel quantMetricLevel;

//	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
//	@JoinColumn(name = "quant_metric_id")
//	private Set<MetricVariables> metricVariables = new HashSet<>();

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name = "quant_metric_id")
	private Set<MetricFormulaItems> metricFormulaItems = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "quant_metrics_to_data_type_classifications",
		joinColumns = {@JoinColumn(name = "quant_metric_id")},
		inverseJoinColumns = {@JoinColumn(name = "data_type_id")}
	)
	private Set<DataTypeClassification> dataTypeClassifications = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "quant_metrics_to_technology_categories",
		joinColumns = {@JoinColumn(name = "quant_metric_id")},
		inverseJoinColumns = {@JoinColumn(name = "technology_category_id")}
	)
	private Set<TechnologyCategories> technologyCategories = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "quant_metrics_to_technologies",
		joinColumns = {@JoinColumn(name = "quant_metric_id")},
		inverseJoinColumns = {@JoinColumn(name = "technology_id")}
	)
	private Set<Technologies> technologies = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "quant_metrics_to_regulations",
		joinColumns = {@JoinColumn(name = "quant_metric_id")},
		inverseJoinColumns = {@JoinColumn(name = "regulation_id")}
	)
	private Set<Regulations> regulations = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by_id")
	private Users createdBy;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "updated_by_id")
	private Users updatedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at")
	private Date updatedAt;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "quant_metrics_to_industries",
		joinColumns = {@JoinColumn(name = "quant_metric_id")},
		inverseJoinColumns = {@JoinColumn(name = "industry_id")}
	)
	private Set<Industries> industries = new HashSet<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "deployment_type")
	private DeploymentType deploymentType;

	@Column(name = "measurement_unit")
	private String measurementUnit;

	@Column(name = "unit_ui_position")
	private String unitUIPosition;

}
