package com.cyberintech.vrisk.server.model.dto.dashboards;

import com.cyberintech.vrisk.server.model.jpa.domains.DashboardItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dashboard Item Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-13
 */
@Setter
@Getter
public class DashboardTableItemDTO extends DashboardItemDTO {

	@Schema
	private List<List<DashboardDataItemDTO>> gridItems;

	@Schema
	private Boolean searchAble;

	/**
	 * Default constructor
	 */
	public DashboardTableItemDTO() {
		super();
		gridItems = new ArrayList<>();
		setDashboardItemType(DashboardItemType.Table);
	}

	@java.beans.ConstructorProperties({"id", "name"})
	public DashboardTableItemDTO(Long id, String name) {
		super(id, name, null, DashboardItemType.Table);

		gridItems = new ArrayList<>();
	}

	public static DashboardTableItemDTO of(Long id, String name, Boolean searchAble) {
		DashboardTableItemDTO result = new DashboardTableItemDTO(id, name);
		result.setSearchAble(searchAble);

		return result;
	}

	/**
	 * Add Grid Items from list of strings
	 *
	 * @param items
	 * @param sortable
	 */
	public void addGridHeaders(List<String> items, boolean sortable) {
		if (items != null) {
			List<DashboardDataItemDTO> itemsList = items.stream().map(value -> {
				DashboardDataItemDTO result = new DashboardDataItemDTO(value, true);
				result.setTextAlign("center");
				result.setSortable(sortable);
				return result;
			}).collect(Collectors.toList());
			gridItems.add(itemsList);
		}
	}

	/**
	 * Add Grid Headers from list of strings
	 *
	 * @param items
	 */
	public void addGridHeaders(List<String> items) {
		addGridHeaders(items, true);
	}

	/**
	 * Add Grid Items from list of strings
	 *
	 * @param items
	 */
	public void addGridItems(List<String> items) {
		if (items != null) {
			List<DashboardDataItemDTO> itemsList = items.stream().map(DashboardDataItemDTO::new).collect(Collectors.toList());
			gridItems.add(itemsList);
		}
	}

	/**
	 * Add Grid Items from list of strings
	 *
	 * @param items
	 */
	public void addGridItems(List<String> items, String textAlign, String symbol) {
		if (items != null) {
			List<DashboardDataItemDTO> itemsList = items.stream().map(value -> {
				DashboardDataItemDTO result = new DashboardDataItemDTO(value);
				result.setTextAlign(textAlign);
				result.setSymbol(symbol);

				return result;
			}).collect(Collectors.toList());
			gridItems.add(itemsList);
		}
	}

}
