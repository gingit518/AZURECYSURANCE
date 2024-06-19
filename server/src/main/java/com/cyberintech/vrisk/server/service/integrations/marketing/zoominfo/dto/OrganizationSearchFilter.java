package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto;

import com.cyberintech.vrisk.server.model.data.BaseFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ZoomInfo Organization search request
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-03-22
 */
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class OrganizationSearchFilter extends BaseFilter {

	@Schema
	private Long companyId;

	@Schema
	private String companyName;

	/**
	 * Default constructor
	 */
	public OrganizationSearchFilter() {
	}

	/**
	 * Company based constructor
	 */
	public OrganizationSearchFilter(Long companyId) {
		this.companyId = companyId;
	}

}
