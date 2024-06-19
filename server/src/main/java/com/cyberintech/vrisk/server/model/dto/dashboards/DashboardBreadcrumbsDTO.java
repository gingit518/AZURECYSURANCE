package com.cyberintech.vrisk.server.model.dto.dashboards;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Dashboard Breadcrumbs Item Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-02-05
 */
@Setter
@Getter
@ToString(of = {"id", "title"})
@EqualsAndHashCode(of = {"id"})
public class DashboardBreadcrumbsDTO {

	@Schema
	private String id;

	@Schema
	private String title;

	@Schema
	private String link;

	@Schema
	private String href;

	/**
	 * Default constructor
	 */
	public DashboardBreadcrumbsDTO() {
	}

	@java.beans.ConstructorProperties({"id", "name", "link", "href"})
	public DashboardBreadcrumbsDTO(String id, String title, String link, String href) {
		this();
		this.id = id;
		this.title = title;
		this.link = link;
		this.href = href;
	}

	@java.beans.ConstructorProperties({"id", "name", "link"})
	public DashboardBreadcrumbsDTO(String id, String title, String link) {
		this(id, title, link, null);
	}

	public static DashboardBreadcrumbsDTO of(String id, String title, String link, String href) {
		return new DashboardBreadcrumbsDTO(id, title, link, href);
	}

	public static DashboardBreadcrumbsDTO of(String id, String title, String link) {
		return new DashboardBreadcrumbsDTO(id, title, link);
	}

}
