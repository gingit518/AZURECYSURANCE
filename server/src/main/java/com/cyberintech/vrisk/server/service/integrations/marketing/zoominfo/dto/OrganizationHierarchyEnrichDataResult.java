package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * ZoomInfo Organization Hierarchy Enrich data result
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-03-31
 */
@Setter
@Getter
public class OrganizationHierarchyEnrichDataResult {

	@Schema
	private Map<String, Object> input;

	@Schema
	private List<OrganizationHierarchyEnrichItem> data;

	/**
	 * Default constructor
	 */
	public OrganizationHierarchyEnrichDataResult() {
	}

}
