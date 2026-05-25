package com.cyberintech.vrisk.server.model.dto.dashboards;

import com.cyberintech.vrisk.server.model.jpa.domains.DashboardItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Dashboard Item Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2026-05-23
 */
@Setter
@Getter
public class DashboardCheckStatusItemDTO extends DashboardItemDTO {

	@Schema
	private Integer requirement;

	@Schema
	private Integer value;

	@Schema
	private String action;

	/**
	 * Default constructor
	 */
	public DashboardCheckStatusItemDTO() {
		super();
		setDashboardItemType(DashboardItemType.CheckStatus);
	}

	@java.beans.ConstructorProperties({"id", "name"})
	public DashboardCheckStatusItemDTO(Long id, String name) {
		super(id, name, null, DashboardItemType.CheckStatus);
	}

	public static DashboardCheckStatusItemDTO of(Long id, String name, Integer requirement, Integer value, String action) {
		DashboardCheckStatusItemDTO result = new DashboardCheckStatusItemDTO(id, name);
		result.setRequirement(requirement);
		result.setValue(value);
		result.setAction(action);

		return result;
	}

}
