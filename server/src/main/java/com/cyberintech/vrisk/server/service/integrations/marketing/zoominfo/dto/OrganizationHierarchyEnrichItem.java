package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * ZoomInfo Organization Hierarchy enrich item
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-03-31
 */
@Setter
@Getter
public class OrganizationHierarchyEnrichItem extends DTOBase<Object> {

	public static String[] OUTPUT_COLUMNS = new String[]{
		"parentage",
		"familyTree",
		"companyId"
	};

	@Schema
	private Long companyId;

	@Schema
	private List<Object> parentage;

	@Schema
	private List<OrganizationHierarchyEnrichTreeNodeItem> familyTree;

	/**
	 * Default constructor
	 */
	public OrganizationHierarchyEnrichItem() {
	}

}
