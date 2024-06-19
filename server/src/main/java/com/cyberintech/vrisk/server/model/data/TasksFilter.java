package com.cyberintech.vrisk.server.model.data;

import lombok.*;

/**
 * Implementation of Tasks Filtering Logic
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-05-14
 */
@NoArgsConstructor
@Setter
@Getter
public class TasksFilter extends NameFilter {
	private Long taskAssigneeId;
	private Long taskManagerId;
	private Long taskManagerOrAssigneeId;
}
