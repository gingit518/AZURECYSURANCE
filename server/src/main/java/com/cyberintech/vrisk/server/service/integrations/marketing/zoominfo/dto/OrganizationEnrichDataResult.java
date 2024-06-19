package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * ZoomInfo Organization Enrich data result
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-03-22
 */
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class OrganizationEnrichDataResult {

	@Schema
	private Map<String, Object> input;

	@Schema
	private List<OrganizationEnrichItem> data;

	/**
	 * Default constructor
	 */
	public OrganizationEnrichDataResult() {
	}

}
