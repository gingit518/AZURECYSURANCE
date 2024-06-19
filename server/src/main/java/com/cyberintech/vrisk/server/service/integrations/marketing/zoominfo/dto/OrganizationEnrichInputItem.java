package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * ZoomInfo Organization enrich search item
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
public class OrganizationEnrichInputItem extends DTOBase<Object> {

	@Schema
	private Long companyId;

	@Schema
	private String companyName;

	/**
	 * Default constructor
	 */
	public OrganizationEnrichInputItem() {
	}

	/**
	 * Static constructor for parameters
	 *
	 * @param companyId
	 * @param companyName
	 * @return
	 */
	public static OrganizationEnrichInputItem of (Long companyId, String companyName) {
		OrganizationEnrichInputItem result = new OrganizationEnrichInputItem();
		if (companyId != null && companyId > 0)result.setCompanyId(companyId);
		if (StringUtils.isNotEmpty(companyName)) result.setCompanyName(companyName);

		return result;
	}

	/**
	 * Static constructor for the filter
	 * @param filter
	 * @return
	 */
	public static OrganizationEnrichInputItem of (OrganizationSearchFilter filter) {
		OrganizationEnrichInputItem result = new OrganizationEnrichInputItem();
		if (filter.getCompanyId() != null && filter.getCompanyId() > 0) result.setCompanyId(filter.getCompanyId());
		if (StringUtils.isNotEmpty(filter.getCompanyName())) result.setCompanyName(filter.getCompanyName());

		return result;
	}

}
