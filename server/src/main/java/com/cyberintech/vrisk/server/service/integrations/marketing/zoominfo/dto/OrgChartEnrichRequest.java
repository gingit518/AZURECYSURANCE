package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrgChartEnrichRequest extends DTOBase<Object> {

	@Schema
	private String companyId;

	@Schema
	private Long rpp;

	@Schema
	private Long page;


	/**
	 * Default constructor
	 */
	public OrgChartEnrichRequest() {
	}

	/**
	 * Static constructor for the filter
	 *
	 * @param companyId
	 * @param page
	 * @param rpp
	 * @return
	 */
	public static OrgChartEnrichRequest of (String companyId, Long page, Long rpp) {
		OrgChartEnrichRequest result = new OrgChartEnrichRequest();
		result.setCompanyId(companyId);
		result.setPage(page);
		result.setRpp(rpp);

		return result;
	}

}
