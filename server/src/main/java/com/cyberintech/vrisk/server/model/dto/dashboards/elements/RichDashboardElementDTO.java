package com.cyberintech.vrisk.server.model.dto.dashboards.elements;

import com.cyberintech.vrisk.server.model.dto.dashboards.DashboardItemDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Dashboard Grid Item Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-12-19
 */
@Setter
@Getter
@ToString(of = {"value", "color"})
public class RichDashboardElementDTO extends BaseDashboardElementDTO<DashboardItemDTO> {

	public RichDashboardElementDTO() {
		super();
	}

	public RichDashboardElementDTO(DashboardItemDTO value) {
		super(value);
	}
}
