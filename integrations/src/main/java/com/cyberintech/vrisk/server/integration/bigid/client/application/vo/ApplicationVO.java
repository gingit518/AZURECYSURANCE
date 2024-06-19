package com.cyberintech.vrisk.server.integration.bigid.client.application.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data

public class ApplicationVO {
	@JsonProperty("_id")
	private String idInternal;
	@JsonProperty("app_account_name")
	private String appAccountName;
	private String location;
	@JsonProperty("owner_email")
	private String ownerEmail;
	@JsonProperty("owner_name")
	private String ownerName;
	@JsonProperty("owner_phone")
	private String ownerPhone;
	@JsonProperty("security_tier")
	private String securityTier;
	@JsonProperty("data_mapping_classifier")
	private String dataMappingClassifier;
	@JsonProperty("source_ip")
	private String sourceIp;
	@JsonProperty("target_data_source")
	private String targetDataSource;
	private String filter;
	private String name;
	private String version;
}
