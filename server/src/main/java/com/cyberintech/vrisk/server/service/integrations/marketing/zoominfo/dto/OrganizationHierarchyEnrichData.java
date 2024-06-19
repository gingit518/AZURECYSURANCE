package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * ZoomInfo Organization Hierarchy Enrich data item
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-03-31
 */
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class OrganizationHierarchyEnrichData {

	@Schema
	private List<List<String>> outputFields;

	@Schema
	private List<OrganizationHierarchyEnrichDataResult> result;

	/**
	 * Default constructor
	 */
	public OrganizationHierarchyEnrichData() {
	}

}
