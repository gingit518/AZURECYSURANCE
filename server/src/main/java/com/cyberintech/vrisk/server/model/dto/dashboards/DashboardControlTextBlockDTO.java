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
public class DashboardControlTextBlockDTO extends DashboardItemDTO {

	@Schema
	private String title;

	@Schema
	private String label;

	@Schema
	private String borderColor;

	@Schema
	private String titleColor;

	// title, label, borderColor, titleColor

	/**
	 * Default constructor
	 */
	public DashboardControlTextBlockDTO() {
		super();
		setDashboardItemType(DashboardItemType.ControlTextBlock);
	}

	@java.beans.ConstructorProperties({"id", "name"})
	public DashboardControlTextBlockDTO(Long id, String name) {
		super(id, name, null, DashboardItemType.ControlTextBlock);
	}

	public static DashboardControlTextBlockDTO of(Long id, String label, String title, String borderColor, String titleColor) {
		DashboardControlTextBlockDTO result = new DashboardControlTextBlockDTO(id, label);
		result.setLabel(label);
		result.setTitle(title);
		result.setBorderColor(borderColor);
		result.setTitleColor(titleColor);

		return result;
	}

}
