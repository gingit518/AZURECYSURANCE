package com.cyberintech.vrisk.server.model.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Implementation of Framework-based Filtering Logic
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-08-19
 */
@Setter
@Getter
@NoArgsConstructor
public class ByFrameworkFilter extends ByParentFilter {

	private Long frameworkId;

}
