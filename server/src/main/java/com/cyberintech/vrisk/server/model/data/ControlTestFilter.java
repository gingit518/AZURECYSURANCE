package com.cyberintech.vrisk.server.model.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Implementation of Control Tests Filtering Logic
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-02-20
 */
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"name"})
@EqualsAndHashCode(of = {"name"}, callSuper = false)
public class ControlTestFilter extends BaseFilter<Long> {

	public static final String STATUS_ANSWERED = "ANSWERED";
	public static final String STATUS_UNANSWERED = "UNANSWERED";
	public static final String STATUS_BOTH = "BOTH";

	@Schema
	private String name;

	@Schema
	private String status;

}
