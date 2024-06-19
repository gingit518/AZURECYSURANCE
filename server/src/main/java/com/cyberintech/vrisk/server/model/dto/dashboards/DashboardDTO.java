package com.cyberintech.vrisk.server.model.dto.dashboards;

import com.cyberintech.vrisk.server.model.jpa.domains.DashboardType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

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
public class DashboardDTO {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private DashboardType dashboardType;

	@Schema
	private List<DashboardSectionDTO> sections;

	@Schema
	private String referenceUUID;

	/**
	 * Default constructor
	 */
	public DashboardDTO() {
		sections = new ArrayList<>();
	}

	@java.beans.ConstructorProperties({"id", "name", "description", "dashboardType"})
	public DashboardDTO(Long id, String name, String description, DashboardType dashboardType) {
		this();
		this.id = id;
		this.name = name;
		this.description = description;
		this.dashboardType = dashboardType;
	}

}
