package com.cyberintech.vrisk.server.model.dto.dashboards;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard Section Reference Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-08-23
 */
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
public class DashboardSectionRefDTO {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private List<DashboardItemRefDTO> dashboardItems;

	/**
	 * Default constructor
	 */
	public DashboardSectionRefDTO() {
		dashboardItems = new ArrayList<>();
		id = 1L;
	}

	@java.beans.ConstructorProperties({"id", "name"})
	public DashboardSectionRefDTO(Long id, String name) {
		this();
		this.id = id;
		this.name = name;
	}
}
