package com.cyberintech.vrisk.server.model.dto.dashboards;

import com.cyberintech.vrisk.server.model.jpa.domains.DashboardItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dashboard Item Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-02-05
 */
@Setter
@Getter
public class DashboardChartItemDTO extends DashboardTableItemDTO {

	@Schema
	private String xAxis;

	@Schema
	private String yAxis;

	private DashboardChartThresholdDTO threshold;

	/**
	 * Default constructor
	 */
	public DashboardChartItemDTO() {
		super();
	}

	@java.beans.ConstructorProperties({"id", "name", "description", "dashboardType"})
	public DashboardChartItemDTO(Long id, String name, String description, DashboardItemType dashboardItemType) {
		super();

		setId(id);
		setName(name);
		setDescription(description);
		setDashboardItemType(dashboardItemType);
	}

}
