package com.cyberintech.vrisk.server.model.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Implementation of Parent-based Filtering Logic
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-09
 */
@NoArgsConstructor
@Setter
@Getter
public class ByParentFilter extends NameFilter {

	private Long parentId;

}
