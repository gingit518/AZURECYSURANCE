package com.cyberintech.vrisk.server.model.dto.dashboards;

import com.cyberintech.vrisk.server.model.jpa.domains.SLCT;
import com.cyberintech.vrisk.server.util.ClientMessage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard Breadcrumbs Item Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-02-05
 */
public class DashboardBreadcrumbsHelper {

	private final ClientMessage clientMessage;

	@Getter
	List<DashboardBreadcrumbsDTO> breadcrumbs;

	/**
	 * Default constructor
	 */
	public DashboardBreadcrumbsHelper(ClientMessage clientMessage) {
		this.clientMessage = clientMessage;
		breadcrumbs = new ArrayList<>();
	}


	/**
	 * Get ClientMessage
	 *
	 * Return Dashboard Breadcrumbs for Home page
	 */
	public static DashboardBreadcrumbsHelper HOME(ClientMessage clientMessage) {
		DashboardBreadcrumbsHelper result = DASHBOARD(clientMessage);
		//result.getBreadcrumbs().add(DashboardBreadcrumbsDTO.of("ROOT", clientMessage.getMessage(SLCT.MENU$HOME_PAGE), "/private/home"));

		return result;
	}


	/**
	 * Get ClientMessage
	 *
	 * Return Dashboard Breadcrumbs for Home page / Dashboards
	 */
	public static DashboardBreadcrumbsHelper DASHBOARD(ClientMessage clientMessage) {
		DashboardBreadcrumbsHelper result = new DashboardBreadcrumbsHelper(clientMessage);
		result.getBreadcrumbs().add(DashboardBreadcrumbsDTO.of("DASHBOARD", clientMessage.getMessage(SLCT.MENU$DASHBOARDS), "/private/dashboards/3"));

		return result;
	}

	/**
	 * Get ClientMessage
	 *
	 * Return Dashboard Breadcrumbs for Home page / Dashboards / Executive Analytics
	 */
	public static DashboardBreadcrumbsHelper DASHBOARD_EXECUTIVE_ANALYTICS(ClientMessage clientMessage) {
		DashboardBreadcrumbsHelper result = DASHBOARD(clientMessage);
		result.getBreadcrumbs().add(DashboardBreadcrumbsDTO.of("DASHBOARD_EXECUTIVE_ANALYTICS", clientMessage.getMessage("DASHBOARDS$EXECUTIVE_ANALYTICS"), "/private/dashboards/1000000001"));

		return result;
	}

	/**
	 * Get ClientMessage
	 *
	 * Return Dashboard Breadcrumbs for Home page / Dashboards / Set Up Dashboard
	 */
	public static DashboardBreadcrumbsHelper SET_UP_DASHBOARD(ClientMessage clientMessage) {
		DashboardBreadcrumbsHelper result = DASHBOARD(clientMessage);
		result.getBreadcrumbs().add(DashboardBreadcrumbsDTO.of("SET_UP_DASHBOARD", clientMessage.getMessage("DASHBOARD_TABS$SET_UP$NAME"), "/private/dashboards/41"));

		return result;
	}

	/**
	 * Get ClientMessage
	 *
	 * Return Dashboard Breadcrumbs for Home page / Dashboards / Executive Dashboard
	 */
	public static DashboardBreadcrumbsHelper DASHBOARD_EXECUTIVE(ClientMessage clientMessage) {
		DashboardBreadcrumbsHelper result = DASHBOARD(clientMessage);
		result.getBreadcrumbs().add(DashboardBreadcrumbsDTO.of("DASHBOARD_EXECUTIVE", clientMessage.getMessage("DASHBOARDS$EXECUTIVE_DASHBOARDS"), "/private/dashboards/1"));

		return result;
	}

	/**
	 * Get ClientMessage
	 *
	 * Return Dashboard Breadcrumbs for Home page / Dashboards / Vendor Cyber Risk Manager
	 */
	public static DashboardBreadcrumbsHelper VENDOR_CYBER_RISK_MANAGER(ClientMessage clientMessage) {
		DashboardBreadcrumbsHelper result = DASHBOARD(clientMessage);
		result.getBreadcrumbs().add(DashboardBreadcrumbsDTO.of("VENDOR_CYBER_RISK_MANAGER", clientMessage.getMessage("DASHBOARD_TABS$VENDOR_CYBER_RISK_MANAGER$NAME"), "/private/dashboards/135"));

		return result;
	}

	/**
	 * Get ClientMessage
	 *
	 * Return Dashboard Breadcrumbs for Home page / Dashboards / CISO Dashboard
	 */
	public static DashboardBreadcrumbsHelper DASHBOARD_CISO(ClientMessage clientMessage) {
		DashboardBreadcrumbsHelper result = DASHBOARD(clientMessage);
		result.getBreadcrumbs().add(DashboardBreadcrumbsDTO.of("DASHBOARD_CISO", clientMessage.getMessage("DASHBOARDS$CISO_DASHBOARDS"), "/private/dashboards/2"));

		return result;
	}

	/**
	 * Get ClientMessage
	 *
	 * Return Dashboard Breadcrumbs for Home page / Dashboards / DPO Dashboard
	 */
	public static DashboardBreadcrumbsHelper DPO_DASHBOARD(ClientMessage clientMessage) {
		DashboardBreadcrumbsHelper result = DASHBOARD(clientMessage);
		result.getBreadcrumbs().add(DashboardBreadcrumbsDTO.of("DPO_DASHBOARD", clientMessage.getMessage("DASHBOARD_TABS$DPO$NAME"), "/private/dashboards/120"));

		return result;
	}

	/**
	 * Get ClientMessage
	 *
	 * Return Dashboard Breadcrumbs for Home page / Dashboards / Audit Dashboard
	 */
	public static DashboardBreadcrumbsHelper AUDIT_DASHBOARD(ClientMessage clientMessage) {
		DashboardBreadcrumbsHelper result = DASHBOARD(clientMessage);
		result.getBreadcrumbs().add(DashboardBreadcrumbsDTO.of("AUDIT_DASHBOARD", clientMessage.getMessage("DASHBOARDS$AUDIT_DASHBOARD"), "/private/dashboards/1241"));

		return result;
	}

	/**
	 * Get ClientMessage
	 *
	 * Return Dashboard Breadcrumbs for Home page / Dashboards / CFO Dashboard
	 */
	public static DashboardBreadcrumbsHelper CFO_DASHBOARD(ClientMessage clientMessage) {
		DashboardBreadcrumbsHelper result = DASHBOARD(clientMessage);
		result.getBreadcrumbs().add(DashboardBreadcrumbsDTO.of("CFO_DASHBOARD", clientMessage.getMessage("DASHBOARDS$CFO_DASHBOARD"), "/private/dashboards/2001"));

		return result;
	}

	/**
	 * Get ClientMessage
	 *
	 * Return Dashboard Breadcrumbs for Home page / GDPR
	 */
	public static DashboardBreadcrumbsHelper GDPR(ClientMessage clientMessage) {
		//TODO Make single breadcrumbs helper for the whole project
		DashboardBreadcrumbsHelper result = HOME(clientMessage);
		result.getBreadcrumbs().add(DashboardBreadcrumbsDTO.of("GDPR", clientMessage.getMessage("GDPR$GDPR"), "/private/gdpr-compliance/list"));

		return result;
	}

	/**
	 * Get Dashboard Breadcrumbs
	 * Add new point to Dashboard Breadcrumbs
	 * Return Dashboard Breadcrumbs
	 */
	public DashboardBreadcrumbsHelper add(String id, String title, String link) {
		this.getBreadcrumbs().add(DashboardBreadcrumbsDTO.of(id, clientMessage.getMessage(title), link));

		return this;
	}

	/**
	 * Get Dashboard Breadcrumbs
	 * Extend Dashboard Breadcrumbs by one point
	 * Return Dashboard Breadcrumbs
	 */
	public DashboardBreadcrumbsHelper extend(String id, String title, String link) {
		DashboardBreadcrumbsHelper result = new DashboardBreadcrumbsHelper(clientMessage);
		result.getBreadcrumbs().addAll(breadcrumbs);
		result.add(id, title, link);

		return result;
	}

}
