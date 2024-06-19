package com.cyberintech.vrisk.server.service.dashboards.powerbi.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EmbedConfigRequest {

	private List<IdObject> datasets;
	private List<IdObject> reports;
	private List<IdObject> targetWorkspaces;

	public EmbedConfigRequest() {
		datasets = new ArrayList<>();
		reports = new ArrayList<>();
		targetWorkspaces = new ArrayList<>();
	}

	@Data
	public static class IdObject {
		String id;

		public IdObject(String id) {
			this.id = id;
		}
	}
}
