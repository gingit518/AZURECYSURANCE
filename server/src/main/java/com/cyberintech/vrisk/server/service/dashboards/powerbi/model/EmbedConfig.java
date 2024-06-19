package com.cyberintech.vrisk.server.service.dashboards.powerbi.model;

import lombok.Data;

import java.util.List;

@Data
public class EmbedConfig {
	private List<ReportConfig> embedReports;
	private EmbedToken embedToken;
	private String errorMessage;
}
