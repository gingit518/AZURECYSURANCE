package com.cyberintech.vrisk.server.model.dto.dashboards;

import com.cyberintech.vrisk.server.model.jpa.domains.DashboardItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * Dashboard Item Reference Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-08-23
 */
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
public class DashboardItemRefDTO {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private DashboardItemType dashboardItemType;

	@Schema
	private DashboardItemFilterDTO dashboardItemFilter;

	@Schema
	private Map<String, Object> parameters;

	/**
	 * Default constructor
	 */
	public DashboardItemRefDTO() {
		parameters = new HashMap<>();
	}

	@java.beans.ConstructorProperties({"id", "name", "dashboardType"})
	public DashboardItemRefDTO(Long id, String name, DashboardItemType dashboardItemType) {
		this();
		this.id = id;
		this.name = name;
		this.dashboardItemType = dashboardItemType;
	}

	/**
	 * Add parameter to the object
	 *
	 * @param name
	 * @param value
	 */
	public void addParameter(String name, Object value) {
		if (parameters == null) {
			parameters = new HashMap<>();
		}

		parameters.put(name, value);
	}

	/**
	 * Static constructor
	 *
	 * @param id
	 * @param name
	 * @param dashboardItemType
	 * @return
	 */
	public static DashboardItemRefDTO of(Long id, String name, DashboardItemType dashboardItemType) {
		return new DashboardItemRefDTO(id, name, dashboardItemType);
	}

}
