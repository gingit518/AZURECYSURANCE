package com.cyberintech.vrisk.server.model.dto.dashboards;

import com.cyberintech.vrisk.server.model.jpa.domains.DashboardFilterType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * Dashboard Item Filter Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-08-23
 */
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
public class DashboardItemFilterDTO {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private DashboardFilterType dashboardFilterType;

	@Schema
	private Map<String, Object> parameters;

	@Schema
	private Map<String, Object> values;

	/**
	 * Default constructor
	 */
	public DashboardItemFilterDTO() {
		parameters = new HashMap<>();
		values = new HashMap<>();
	}

	@java.beans.ConstructorProperties({"id", "name", "description", "dashboardFilterType", "parameters", "values"})
	public DashboardItemFilterDTO(Long id, String name, String description, DashboardFilterType dashboardFilterType, Map<String, Object> parameters, Map<String, Object> values) {
		this();
		this.id = id;
		this.name = name;
		this.description = description;
		this.dashboardFilterType = dashboardFilterType;
		this.parameters = parameters;
		this.values = values;
	}

	public DashboardItemFilterDTO(Long id, String name, String description, DashboardFilterType dashboardFilterType) {
		this();
		this.id = id;
		this.name = name;
		this.description = description;
		this.dashboardFilterType = dashboardFilterType;
	}

	/**
	 * Get Long Value from the Filter
	 *
	 * @param filterName
	 * @param fieldName
	 * @return
	 */
	public Long getValueLong(String filterName, String fieldName) {
		Map<String, Object> questionMap = (Map) getValues().get(filterName);
		Integer resultInt = (Integer) questionMap.get(fieldName);

		return resultInt != null ? resultInt.longValue() : null;
	}

	/**
	 * Returns true if any value initialized
	 *
	 * @return
	 */
	public boolean hasValues() {
		return getValues() != null && getValues().size() > 0;
	}

}
