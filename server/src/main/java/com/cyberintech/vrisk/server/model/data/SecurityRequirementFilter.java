package com.cyberintech.vrisk.server.model.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Implementation of Security Requirement Filtering Logic
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020.01.29
 */
@NoArgsConstructor
@Setter
@Getter
public class SecurityRequirementFilter extends NameFilter {

	private Long securityControlFamilyId;

	private Long securityControlNameId;

	private Long systemId;

	private Long assessmentLevelId;

}
