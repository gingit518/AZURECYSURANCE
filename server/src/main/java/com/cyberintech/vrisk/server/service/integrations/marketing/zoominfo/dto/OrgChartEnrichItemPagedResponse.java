package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * ZoomInfo Ogr Chart enrich items paged response
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-03-29
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrgChartEnrichItemPagedResponse extends PagedSearchResults<OrgChartEnrichItem> {

	/**
	 * Default constructor
	 */
	public OrgChartEnrichItemPagedResponse() {
	}

}
