package com.cyberintech.vrisk.server.model.dto.dashboards.elements;

import com.cyberintech.vrisk.server.model.dto.dashboards.DashboardItemDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.DashboardItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Dashboard Grid Layout Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-12-19
 */
@Setter
@Getter
public class DashboardGridLayoutDTO extends DashboardItemDTO {

	@Schema
	private List<List<RichDashboardElementDTO>> gridItems;

	/**
	 * Default constructor
	 */
	public DashboardGridLayoutDTO() {
		setDashboardItemType(DashboardItemType.GridLayout);
		gridItems = new ArrayList<>();
	}

	@java.beans.ConstructorProperties({"id", "name", "description"})
	public DashboardGridLayoutDTO(Long id, String name, String description) {
		super(id, name, description, DashboardItemType.GridLayout);

		gridItems = new ArrayList<>();
	}

	@java.beans.ConstructorProperties({"id", "name"})
	public DashboardGridLayoutDTO(Long id, String name) {
		super(id, name, null, DashboardItemType.GridLayout);

		gridItems = new ArrayList<>();
	}

	public void addRowItems(RichDashboardElementDTO ...items) {
		if (items != null && items.length > 0) {
			List<RichDashboardElementDTO> rowItems = new ArrayList<RichDashboardElementDTO>(Arrays.asList(items));

			gridItems.add(rowItems);
		}
	}

}
