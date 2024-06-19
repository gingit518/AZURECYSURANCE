package com.cyberintech.vrisk.server.model.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Implementation of Question Filtering Logic
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-04
 */
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"question"})
@EqualsAndHashCode(of = {"question"}, callSuper = false)
public class QuestionFilter extends BaseFilter<Long> {

	public static final String STATUS_ANSWERED = "ANSWERED";
	public static final String STATUS_UNANSWERED = "UNANSWERED";
	public static final String STATUS_BOTH = "BOTH";

	@Schema
	private String question;

	@Schema
	private String status;

	@Schema
	private String vendorType;

	@Schema
	private String metricDomain;

	@JsonIgnore
	private Long riskModelId;
}
