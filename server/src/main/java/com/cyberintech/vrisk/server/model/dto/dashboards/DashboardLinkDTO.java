package com.cyberintech.vrisk.server.model.dto.dashboards;

import com.cyberintech.vrisk.server.model.jpa.domains.MetricDomain;
import com.cyberintech.vrisk.server.model.jpa.domains.QuantsDomain;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * Dashboard Drill Down
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-02-13
 */
@Setter
@Getter
@ToString(of = {"url"})
public class DashboardLinkDTO {

	@Schema
	private String url;

	@Schema
	private Map<String, String> params;

	@Schema
	private String linkType;

	public DashboardLinkDTO() {
	}

	public DashboardLinkDTO(String url) {
		this.url = url;
	}

	/**
	 * Create link
	 *
	 * @param url
	 * @return
	 */
	public static DashboardLinkDTO of(String url) {
		return of(url, null);
	}

	/**
	 * Create link
	 *
	 * @param url
	 * @param params
	 * @return
	 */
	public static DashboardLinkDTO of(String url, Map<String, String> params) {
		DashboardLinkDTO result = new DashboardLinkDTO();
		result.setUrl(url);
		result.setParams(params);

		return result;
	}

}
