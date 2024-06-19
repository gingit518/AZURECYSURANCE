package com.cyberintech.vrisk.server.integration.bigid.client.objectdetails.vo;

import com.cyberintech.vrisk.server.integration.bigid.client.common.datacatalog.OpenAccess;
import com.cyberintech.vrisk.server.integration.bigid.client.common.datacatalog.tag.TagVO;
import com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo.OwnerVO;
import com.cyberintech.vrisk.server.integration.bigid.client.objectdetails.vo.datasource.DatasourceVO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Optional;

@Data
public class ObjectDetailsVO {
	@JsonProperty("_id")
	private String idInternal;
	private String fullyQualifiedName;
	private String scanId;
	private String scanStatus;
	private String collectionOrTableName;
	private String containerName;
	@JsonProperty("correlation_status")
	private String correlationStatus;
	private String dataSourceName;
	private long estimatedRows;
	@JsonProperty("is_encrypted")
	private boolean encrypted;
	@JsonProperty("is_view")
	private boolean view;
	private String objectName;
	private String owner;
	private long scannedSize;
	private String scannerType;
	private long sizeInBytes;
	private String source;
	private List<TagVO> tags;
	private long totalEnrichmentFindings;
	private long totalFindings;
	private long totalRows;
	private long totalRowsWithFindings;
	@JsonProperty("total_pii_count")
	private long totalPiiCount;
	@JsonProperty("was_scanned")
	private boolean wasScanned;
	@JsonProperty("total_findings_inserted")
	private long totalFindinsInserted;
	private String enrichmentStatus;
	private String type;
	@JsonProperty("country")
	private List<String> countries;
	@JsonProperty("id_source")
	private List<String> idSources;
	@JsonProperty("max_pii_count")
	private long maxPiiCount;
	@JsonProperty("num_identities")
	private long numIdentities;
	@JsonProperty("open_access")
	private OpenAccess openAccess;
	private String fullObjectName;
	@JsonProperty("ds_owners")
	private List<OwnerVO> dsOwners;
	@JsonProperty("ds_location")
	private String dsLocation;

	@JsonProperty("ds")
	private List<DatasourceVO> datasource;

	private List<String> applications;

	@JsonIgnore
	public String getDsName() {
		return Optional.ofNullable(datasource).filter(CollectionUtils::isNotEmpty).map(l -> l.get(0)).map(DatasourceVO::getName).orElse(null);
	}
}
