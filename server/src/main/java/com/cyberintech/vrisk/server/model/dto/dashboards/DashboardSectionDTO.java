package com.cyberintech.vrisk.server.model.dto.dashboards;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard Section Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-02-05
 */
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
public class DashboardSectionDTO {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private List<DashboardItemDTO> dashboardItems;

	@Schema
	private List<DashboardBreadcrumbsDTO> breadcrumbs;

	/**
	 * Default constructor
	 */
	public DashboardSectionDTO() {
		dashboardItems = new ArrayList<>();
		breadcrumbs = new ArrayList<>();

		id = 1L;
	}

	@java.beans.ConstructorProperties({"id", "name", "description"})
	public DashboardSectionDTO(Long id, String name, String description) {
		this();
		this.id = id;
		this.name = name;
		this.description = description;
	}
}
