package com.cyberintech.vrisk.server.model.data;

import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Implementation of Name Filtering Logic
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2013-03-19
 */
@NoArgsConstructor
@Setter
@Getter
public class SubsidiaryOrganizationFilter extends NameFilter {

	private Long parentId;

	private Long topParentId;

	private Boolean isCloudVendor;

	@JsonIgnore
	private OrganizationType organizationType;

}
