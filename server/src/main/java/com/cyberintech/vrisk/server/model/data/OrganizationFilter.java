package com.cyberintech.vrisk.server.model.data;

import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Implementation of Name Filtering Logic
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-04
 */
@NoArgsConstructor
@Setter
@Getter
public class OrganizationFilter extends NameFilter {
	private OrganizationType organizationType;
	private OrganizationRefDTO parent;
	private OrganizationRefDTO rootParent;
	private UserRefDTO owner;
	private Boolean globalOnly;
}
