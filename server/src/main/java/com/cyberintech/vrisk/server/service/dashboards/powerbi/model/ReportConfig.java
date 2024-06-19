package com.cyberintech.vrisk.server.service.dashboards.powerbi.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class ReportConfig {

	private String reportId;

	private String embedUrl;

	private String reportName;

	private String datasetId;

	private Boolean isEffectiveIdentityRolesRequired = false;

	private Boolean isEffectiveIdentityRequired = false;

	private Boolean enableRLS = false;

	private String username;

	private String roles;

}
