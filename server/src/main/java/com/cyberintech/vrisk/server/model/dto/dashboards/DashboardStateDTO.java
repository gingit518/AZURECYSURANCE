package com.cyberintech.vrisk.server.model.dto.dashboards;

import com.cyberintech.vrisk.server.model.jpa.domains.DashboardType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Dashboard Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-08-23
 */
@Setter
@Getter
public class DashboardStateDTO extends DashboardRefDTO {

	@Schema
	private List<DashboardSectionRefDTO> sections;

	@Schema
	private Map<String, Object> parameters;

	@Schema
	private String referenceUUID;

	/**
	 * Default constructor
	 */
	public DashboardStateDTO() {
		super();

		sections = new ArrayList<>();
		parameters = new HashMap<>();
	}

	public DashboardStateDTO(Long id, String name) {
		super(id, name, null, null, null, null, null);

		sections = new ArrayList<>();
		parameters = new HashMap<>();
	}

	public DashboardStateDTO(Long id, String name, String referenceUUID) {
		super(id, name, null, null, null, null, null);

		this.sections = new ArrayList<>();
		this.parameters = new HashMap<>();
		this.referenceUUID = referenceUUID;
	}

	/**
	 * Create reference to dashboard
	 *
	 * @param dashboard
	 * @param referenceUUID
	 * @return
	 */
	public static DashboardStateDTO of(DashboardDTO dashboard, String referenceUUID) {
		DashboardStateDTO result = new DashboardStateDTO();

		if (StringUtils.isEmpty(referenceUUID)) {
			result.setReferenceUUID(UUID.randomUUID().toString());
		} else {
			result.setReferenceUUID(referenceUUID);
		}
		result.setId(dashboard.getId());
		result.setName(dashboard.getName());

		return result;
	}

	@java.beans.ConstructorProperties({"id", "name", "description", "dashboardType", "icon", "parentName"})
	public DashboardStateDTO(Long id, String name, String description, DashboardType dashboardType, String icon, String parentName) {
		super(id, name, description, dashboardType, icon, parentName, null);

		sections = new ArrayList<>();
		parameters = new HashMap<>();
	}

	/**
	 * Get Dashboard item by ID
	 *
	 * @param itemId
	 * @return
	 */
	public DashboardItemRefDTO getItemById(Long itemId) {
		if (sections != null) {
			for (DashboardSectionRefDTO section : sections) {
				if (section.getDashboardItems() != null) {
					for (DashboardItemRefDTO dashboardItem : section.getDashboardItems()) {
						if (itemId.equals(dashboardItem.getId())) {
							return dashboardItem;
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Get Dashboard item Filter by Item ID
	 *
	 * @param itemId
	 * @return
	 */
	public DashboardItemFilterDTO getFilterByItemById(Long itemId) {

		DashboardItemFilterDTO result = null;

		DashboardItemRefDTO itemDetails = getItemById(itemId);
		if (itemDetails != null) {
			result = itemDetails.getDashboardItemFilter();
		}

		return result;
	}
}
