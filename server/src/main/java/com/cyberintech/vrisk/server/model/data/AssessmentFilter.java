package com.cyberintech.vrisk.server.model.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Implementation of Assessment Filtering Logic
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-07-14
 */
@NoArgsConstructor
@Setter
@Getter
public class AssessmentFilter extends NameFilter {

	private Long assessmentLevelId;

	private Long systemId;

}
