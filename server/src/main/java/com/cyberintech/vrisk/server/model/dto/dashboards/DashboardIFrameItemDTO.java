package com.cyberintech.vrisk.server.model.dto.dashboards;

import com.cyberintech.vrisk.server.model.dto.external_analytics.ExternalAnalyticsAccessDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.DashboardItemType;
import com.cyberintech.vrisk.server.model.jpa.domains.ExternalAnalyticsType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard iFrame Item Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-10-08
 */
@Setter
@Getter
public class DashboardIFrameItemDTO extends DashboardItemDTO {

	@Schema
	private String src;

	@Schema
	private String height;

	@Schema
	private String width;

	@Schema
	private ExternalAnalyticsType analyticsType;

	@Schema
	private ExternalAnalyticsAccessDTO accessDetails;

	@Schema
	private List<PreloadUrlCallDTO> preloadCalls;

	/**
	 * Default constructor
	 */
	public DashboardIFrameItemDTO() {
		super();

		preloadCalls = new ArrayList<>();

		setDashboardItemType(DashboardItemType.iFrame);
	}

	@java.beans.ConstructorProperties({"id", "name", "description"})
	public DashboardIFrameItemDTO(Long id, String name, String description) {
		this();

		setId(id);
		setName(name);
		setDescription(description);
	}

}
