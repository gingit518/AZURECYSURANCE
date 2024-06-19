package com.cyberintech.vrisk.server.model.dto.external_analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * External Analytics Token
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-10-13
 */
@Setter
@Getter
@NoArgsConstructor
public class ExternalAnalyticsTokenDTO {

	@Schema
	private String token;

	/**
	 * Entity based constructor
	 *
	 * @param token
	 */
	public ExternalAnalyticsTokenDTO(String token) {
		this.token = token;
	}

}
