package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * ZoomInfo Organization Hierarchy tree node item
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-03-31
 */
@Setter
@Getter
public class OrganizationHierarchyEnrichTreeNodeItem extends DTOBase<Object> {

	@Schema
	private Long companyId;

	@Schema
	private String name;

	@Schema
	private String city;

	@Schema
	private String state;

	@Schema
	private Map<String, Object> subUnitTypeInfo;

	@Schema
	private List<OrganizationHierarchyEnrichTreeNodeItem> familyNodes;

	/**
	 * Default constructor
	 */
	public OrganizationHierarchyEnrichTreeNodeItem() {
	}

}
