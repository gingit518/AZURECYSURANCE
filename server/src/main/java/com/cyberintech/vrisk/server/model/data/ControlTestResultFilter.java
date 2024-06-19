package com.cyberintech.vrisk.server.model.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Implementation of Control Test Result Filtering Logic
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020.07.10
 */
@NoArgsConstructor
@Setter
@Getter
public class ControlTestResultFilter extends SecurityRequirementFilter {

	private Long assessmentId;

}
