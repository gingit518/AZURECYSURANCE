package com.cyberintech.vrisk.server.model.dto.dashboards;

import com.cyberintech.vrisk.server.model.dto.audit.ItemTypeDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.DashboardItemType;
import com.cyberintech.vrisk.server.service.dashboards.DashboardDataEvaluator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

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
 * @since    2019-09-13
 */
@Setter
@Getter
public class DashboardDataGridItemDTO extends DashboardItemDTO {

	@Schema
	private List<List<DashboardDataItemDTO>> gridFooters;

	@Schema
	private List<List<DashboardDataItemDTO>> gridHeaders;

	@Schema
	private List<List<DashboardDataItemDTO>> gridItems;

	/**
	 * Default constructor
	 */
	public DashboardDataGridItemDTO() {
		gridHeaders = new ArrayList<>();
		gridItems = new ArrayList<>();
	}

	@java.beans.ConstructorProperties({"id", "name"})
	public DashboardDataGridItemDTO(Long id, String name) {
		super(id, name, null, DashboardItemType.DataGrid);

		gridHeaders = new ArrayList<>();
		gridItems = new ArrayList<>();
	}

	/**
	 * Add Grid Headers from list of strings
	 *
	 * @param items
	 */
	public void addGridHeaders(List<String> items) {
		addGridHeaders(items, false);
	}

	/**
	 * Add Grid Headers from list of strings
	 *
	 * @param items
	 * @param sortable
	 */
	public void addGridHeaders(List<String> items, boolean sortable) {
		if (items != null) {
			long i = 0;
			List<DashboardDataItemDTO> itemsList = new ArrayList<>();
			for (String itemLabel : items) {
				Long sortIndex = sortable ? i : null;
				DashboardDataItemHeaderDTO result = DashboardDataItemHeaderDTO.of(itemLabel, sortIndex);
				result.setTextAlign("center");
				// result.setSortable(sortable);
				itemsList.add(result);

				i++;
			}
			gridHeaders.add(itemsList);
		}
	}

	@Override
	public void evaluate(DashboardDataEvaluator dataEvaluator) {
		if (CollectionUtils.isNotEmpty(gridItems)) {
			for (List<DashboardDataItemDTO> row : gridItems) {
				for (DashboardDataItemDTO dataItem : row) {
					dataItem.evaluate(dataEvaluator);
				}
			}
		}
	}

}
