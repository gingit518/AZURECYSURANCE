package com.cyberintech.vrisk.server.integration.bigid.client.datacatalog.vo;

import com.cyberintech.vrisk.server.integration.bigid.client.common.datacatalog.OpenAccess;
import com.cyberintech.vrisk.server.integration.bigid.client.common.datacatalog.tag.TagVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DataCatalogVO {
	private String id;
	private String fullyQualifiedName;
	private String owner;
	@JsonProperty("total_pii_count")
	private long totalPiiCount;
	private String source;
	private String type;
	private String objectType;
	private String extendedObjectType;
	private OpenAccess openAccess;
	private String fullObjectName;
	private String objectName;
	private String containerName;
	private long sizeInBytes;
	private String language;
	private String location;
	@JsonProperty("application_name")
	private List<String> applicationNames;
	private List<String> ownersList;
	@JsonProperty("ds_owner")
	private List<String> dsOwner;
	private List<TagVO> tags;


}
