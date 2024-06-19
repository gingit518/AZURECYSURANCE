package com.cyberintech.vrisk.server.model.dto.dashboards;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dashboard Grid Item Header Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-16
 */
@Setter
@Getter
public class DashboardDataItemHeaderDTO extends DashboardDataItemDTO {

	private Long sortIndex;

	// private boolean sortable = false;

	public DashboardDataItemHeaderDTO() {
		super();
	}

	/**
	 * Apply as Sort Index
	 *
	 * @param sortIndex
	 * @return
	 */
	public DashboardDataItemHeaderDTO applySortIndex(Long sortIndex) {
		setSortIndex(sortIndex);

		return this;
	}

	public static DashboardDataItemHeaderDTO of(String name, Long sortIndex) {
		DashboardDataItemHeaderDTO result = new DashboardDataItemHeaderDTO();
		result.setValue(name);
		if (sortIndex != null) {
			result.setSortIndex(sortIndex);
			result.setSortable(true);
		} else {
			result.setSortable(false);
		}

		return result;
	}

}
