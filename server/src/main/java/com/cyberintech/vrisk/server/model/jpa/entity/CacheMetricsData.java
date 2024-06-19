package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

/**
 * Risk Metrics Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.0
 * @since    2021-03-10
 */
@Entity
@Table(name = "cache_metrics_data")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "organizationName", "systemName", "metricName"})
public class CacheMetricsData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@Column(name = "organization_name")
	private String organizationName;

	@Column(name = "risk_model_id")
	private Long riskModelId;

	@Column(name = "risk_model_name")
	private String riskModelName;

	@Column(name = "system_id")
	private Long systemId;

	@Column(name = "system_name")
	private String systemName;

	@Column(name = "vendor_id")
	private Long vendorId;

	@Column(name = "vendor_name")
	private String vendorName;

	@Column(name = "is_cloud_vendor")
	private Boolean isCloudVendor;

	@Column(name = "is_technology_vendor")
	private Boolean isTechnologyVendor;

	@Column(name = "is_system_vendor")
	private Boolean isSystemVendor;

	@Column(name = "is_service_vendor")
	private Boolean isServiceVendor;

	@Column(name = "system_number_of_records")
	private Long systemNumberOfRecords;

	@Column(name = "data_asset_classification_name")
	private String dataAssetClassificationName;

	@Column(name = "data_class_ids")
	private String dataClassIds;

	@Column(name = "data_class_names")
	private String dataClassNames;

	@Column(name = "business_unit_id")
	private Long businessUnitId;

	@Column(name = "business_unit_name")
	private String businessUnitName;

	@Column(name = "process_id")
	private Long processId;

	@Column(name = "metric_id")
	private Long metricId;

	@Column(name = "metric_name")
	private String metricName;

	@Column(name = "metric_formula")
	private String metricFormula;

	@Column(name = "metric_type")
	private String metricType;

	@Column(name = "metric_level")
	private String metricLevel;

	@Column(name = "metric_value")
	private Double metricValue;

	@Column(name = "metric_domain_id")
	private Long metricDomainId;

	@Column(name = "metric_domain_name")
	private String metricDomainName;

	@Column(name = "regulations")
	private String regulations;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

}
