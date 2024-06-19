package com.cyberintech.vrisk.server.model.dto.dashboards;

import com.cyberintech.vrisk.server.model.jpa.domains.DashboardType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * Dashboard Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-02-05
 */
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
public class DashboardRefDTO {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private DashboardType dashboardType;

	@Schema
	private String icon;

	@Schema
	private String parentName;

	/**
	 * Default constructor
	 */
	public DashboardRefDTO() {
	}

	@java.beans.ConstructorProperties({"id", "name", "description", "dashboardType", "icon"})
	public DashboardRefDTO(Long id, String name, String description, DashboardType dashboardType, String icon) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.dashboardType = dashboardType;
		this.icon = icon;
		this.parentName = null;
	}

	@java.beans.ConstructorProperties({"id", "name", "description", "dashboardType", "icon", "parentName"})
	public DashboardRefDTO(Long id, String name, String description, DashboardType dashboardType, String icon, String parentName, Map<Long, String> dashboardMenuNames) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.dashboardType = dashboardType;
		this.icon = icon;
		this.parentName = parentName;

		if (dashboardMenuNames != null && dashboardMenuNames.containsKey(id)) {
			this.parentName = dashboardMenuNames.get(id);
		}
	}
}
