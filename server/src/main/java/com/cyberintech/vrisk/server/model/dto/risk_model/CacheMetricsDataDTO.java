package com.cyberintech.vrisk.server.model.dto.risk_model;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.domains.QuantMetricLevel;
import com.cyberintech.vrisk.server.model.jpa.entity.CacheMetricsData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Cache Metrics Data Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.0
 * @since    2021-06-12
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "organizationName", "systemName", "metricName"})
public class CacheMetricsDataDTO extends DTOBase<CacheMetricsData> {

	private Long id;

	private Long organizationId;

	private String organizationName;

	private Long riskModelId;

	private String riskModelName;

	private Long systemId;

	private String systemName;

	private Long vendorId;

	private String vendorName;

	private Boolean isCloudVendor;

	private Boolean isTechnologyVendor;

	private Boolean isSystemVendor;

	private Boolean isServiceVendor;

	private Long systemNumberOfRecords;

	private String dataAssetClassificationName;

	private String dataClassIds;

	private String dataClassNames;

	private Long businessUnitId;

	private String businessUnitName;

	private Long processId;

	private Long metricId;

	private String metricName;

	private String metricFormula;

	private String metricType;

	private String metricLevel;

	private Double metricValue;

	private Long metricDomainId;

	private String metricDomainName;

	private Date createdAt;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public CacheMetricsDataDTO(CacheMetricsData entity) {
		super(entity);
	}

}
