package com.cyberintech.vrisk.server.model.dto.dashboards;

import com.cyberintech.vrisk.server.model.jpa.domains.DashboardItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

import java.util.Map;

/**
 * Preload URL calls for iFrame Item
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-10-19
 */
@Setter
@Getter
public class PreloadUrlCallDTO {

	@Schema
	private String url;

	@Schema
	private HttpMethod method = HttpMethod.GET;

	@Schema
	private Map<String, String> headers;

	@Schema
	private Map<String, String> values;

	/**
	 * Default constructor
	 */
	public PreloadUrlCallDTO() {
	}

}
