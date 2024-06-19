package com.cyberintech.vrisk.server.model.dto.external_analytics;

import com.cyberintech.vrisk.server.model.jpa.domains.ExternalAnalyticsType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * External Analytics access Details
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-10-17
 */
@Setter
@Getter
@ToString(of = {"id", "token", "externalAnalyticsType"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class ExternalAnalyticsAccessDTO {

	@Schema
	private Long userId;

	@Schema
	private String token;

	@Schema
	private String tokenType;

	@Schema
	private String tenant;

	@Schema
	private String applicationId;

	private String workspaceId;

	private String reportId;

	private String pageId;

	@Schema
	private String webIntegrationId;

	@Schema
	private String sessionId;

	@Schema
	private ExternalAnalyticsType externalAnalyticsType;

	/**
	 * Default constructor
	 */
	public ExternalAnalyticsAccessDTO() {
	}

}
