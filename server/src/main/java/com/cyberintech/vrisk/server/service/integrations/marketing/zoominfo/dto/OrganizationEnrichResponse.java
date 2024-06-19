package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ZoomInfo search item
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-03-22
 */
@Setter
@Getter
@EqualsAndHashCode
public class OrganizationEnrichResponse extends DTOBase<Object> {

	@Schema
	private Boolean success;

	@Schema
	private OrganizationEnrichData data;

	/**
	 * Default constructor
	 */
	public OrganizationEnrichResponse() {
	}

}
