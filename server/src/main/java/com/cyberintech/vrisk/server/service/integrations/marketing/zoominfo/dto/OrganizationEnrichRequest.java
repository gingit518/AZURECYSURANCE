package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class OrganizationEnrichRequest implements Serializable {

	@Schema
	private List<OrganizationEnrichInputItem> matchCompanyInput;

	@Schema
	private List<String> outputFields;

	/**
	 * Default constructor
	 */
	public OrganizationEnrichRequest() {
		matchCompanyInput = new ArrayList<>();
		outputFields = new ArrayList<>();
	}

	/**
	 * Static constructor for the filter
	 *
	 * @param filter
	 * @return
	 */
	public static OrganizationEnrichRequest of (OrganizationSearchFilter filter, List<String> outputFields) {
		OrganizationEnrichRequest result = new OrganizationEnrichRequest();
		result.getMatchCompanyInput().add(OrganizationEnrichInputItem.of(filter));
		result.getOutputFields().addAll(outputFields);

		return result;
	}

	/**
	 * Static constructor for the filter
	 *
	 * @param filter
	 * @return
	 */
	public static OrganizationEnrichRequest of (OrganizationSearchFilter filter) {
		return of(filter, Arrays.asList(OrganizationEnrichItem.OUTPUT_COLUMNS));
	}

}
