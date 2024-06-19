package com.cyberintech.vrisk.server.model.dto.dashboards;

import com.cyberintech.vrisk.server.model.jpa.domains.MetricDomain;
import com.cyberintech.vrisk.server.model.jpa.domains.QuantsDomain;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
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
@ToString(of = {"view", "params"})
public class DashboardDataItemDrilldownDTO {

	public static final String PARAM_ITEM = "item";
	public static final String PARAM_CATEGORY = "category";
	public static final String PARAM_SUBITEM = "subitem";
	public static final String PARAM_DETAILS = "details";

	public static final String VENDOR_QUALS = "vendor_quals";
	public static final String VENDOR_QUALS_DOMAIN = "vendor_quals_domain";
	public static final String SYSTEM_QUALS = "system_quals";
	public static final String SYSTEM_QUALS_DOMAIN = "system_quals_domain";
	public static final String VENDOR_QUANTS = "vendor_quants";
	public static final String VENDOR_QUANTS_DOMAIN = "vendor_quants_domain";
	public static final String SYSTEM_QUANTS = "system_quants";
	public static final String SYSTEM_QUANTS_DOMAIN = "system_quants_domain";
	public static final String ORGANIZATION_QUANTS = "organization_quants";
	public static final String ADMIN_SYSOWN = "admin_sysown";
	public static final String ADMIN_SYSOWN_QUAL = "admin_sysown_quals";
	public static final String ADMIN_SYSOWN_QUAL_VENDOR = "admin_sysown_quals_vendor";
	public static final String ADMIN_SYSOWN_QUAL_CLOUD = "admin_sysown_quals_cloud";
	public static final String ADMIN_SYSOWN_QUAL_SYSTEM = "admin_sysown_quals_system";
	public static final String ADMIN_SYSOWN_QUANT = "admin_sysown_quant";
	public static final String ADMIN_VNDOWN_QUAL= "admin_vndown_quals_vendor";
	public static final String ADMIN_VNDOWN_QUAL_CLOUD = "admin_vndown_quals_cloud";
	public static final String BUDGET = "budget";
	public static final String VENDOR = "vendor";
	public static final String RESIDUAL_RISK = "residual_risk";
	public static final String TASK = "task";
	public static final String PROJECT = "project";
	public static final String ASSESSMENT = "assessment";
	public static final String SYSTEM_GDPR_STATUS = "system_gdpr_status";

	/**
	 * Categories
	 */
	public static final String CATEGORY_VENDOR_SELF_ASSESSMENT = "vendor_self_assessment";
	public static final String CATEGORY_VENDOR_SELF_ASSESSMENT_QUESTION = "vendor_self_assessment_question";
	public static final String CATEGORY_BUDGET_FIXED_CAPITAL_COSTS = "fixed_capital_costs";
	public static final String CATEGORY_BUDGET_FIXED_OPERATIONAL_COSTS = "fixed_operational_costs";
	public static final String CATEGORY_BUDGET_VARIABLE_OPERATIONAL_COSTS = "variable_operational_costs";

	@Schema
	private String view;

	@Schema
	private Map<String, String> params;

	@Schema
	private VendorType drillDownType;

	public DashboardDataItemDrilldownDTO() {
	}

	public DashboardDataItemDrilldownDTO(String view) {
		this.view = view;
	}

	public DashboardDataItemDrilldownDTO(String view, Map<String, String> params) {
		this.view = view;
		this.params = params;
	}

	/**
	 * Clone Item
	 *
	 * @return
	 */
	public DashboardDataItemDrilldownDTO clone() {
		DashboardDataItemDrilldownDTO result = new DashboardDataItemDrilldownDTO();
		result.setView(getView());
		result.setParams(getParams());

		return result;
	}

	/**
	 * Setter for Drilldown
	 *
	 * @param vendorType
	 */
	public void setDrillDownType(VendorType vendorType) {
		this.drillDownType = vendorType;

		if (this.drillDownType != null) {
			this.getParams().put("vendorName", vendorType.name());
		}
	}

	/**
	 * Drill down type
	 *
	 * @param vendorType
	 * @return
	 */
	public DashboardDataItemDrilldownDTO applyDrillDownType(VendorType vendorType) {
		this.setDrillDownType(vendorType);

		return this;
	}

	/**
	 * Add Param
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	public DashboardDataItemDrilldownDTO param(String name, String value) {
		this.getParams().put(name, value);

		return this;
	}

	/**
	 * Get Dashboard data drilldown for vendor
	 *
	 * @param vendor
	 * @return
	 */
	public static DashboardDataItemDrilldownDTO of(Organizations vendor) {
		return of(vendor, null);
	}

	/**
	 * Get Dashboard data drilldown for vendor
	 *
	 * @param vendor
	 * @param metricDomain
	 * @return
	 */
	public static DashboardDataItemDrilldownDTO of(Organizations vendor, MetricDomain metricDomain) {
		String view = VENDOR_QUALS;
		Map<String, String> params = new HashMap<>();
		params.put("vendor", vendor.getId().toString());
		params.put("vendorName", vendor.getName());
		if (metricDomain != null) {
			view = VENDOR_QUALS_DOMAIN;

			params.put("metricDomain", metricDomain.getCode());
		}

		DashboardDataItemDrilldownDTO result = new DashboardDataItemDrilldownDTO(view, params);

		return result;
	}

	/**
	 * Get Dashboard data drilldown for vendor
	 *
	 * @param vendor
	 * @param metricDomain
	 * @return
	 */
	public static DashboardDataItemDrilldownDTO ofQuant(Organizations vendor, QuantsDomain metricDomain) {
		String view = VENDOR_QUANTS;
		Map<String, String> params = new HashMap<>();
		params.put("vendor", vendor.getId().toString());
		params.put("vendorName", vendor.getName());
		if (metricDomain != null) {
			view = VENDOR_QUANTS_DOMAIN;

			params.put("metricDomain", metricDomain.getId().toString());
		}

		DashboardDataItemDrilldownDTO result = new DashboardDataItemDrilldownDTO(view, params);

		return result;
	}

	/**
	 * Get Dashboard data drilldown for System
	 *
	 * @param system
	 * @return
	 */
	public static DashboardDataItemDrilldownDTO of(Systems system) {
		return of(system, null);
	}

	/**
	 * Get Dashboard data drilldown for System
	 *
	 * @param system
	 * @param metricDomain
	 * @return
	 */
	public static DashboardDataItemDrilldownDTO of(Systems system, MetricDomain metricDomain) {
		String view = SYSTEM_QUALS;
		Map<String, String> params = new HashMap<>();
		params.put("system", system.getId().toString());
		params.put("systemName", system.getName());
		if (metricDomain != null) {
			view = SYSTEM_QUALS_DOMAIN;

			params.put("metricDomain", metricDomain.getCode());
		}

		DashboardDataItemDrilldownDTO result = new DashboardDataItemDrilldownDTO(view, params);

		return result;
	}

	/**
	 * Get Dashboard data drilldown for Task
	 *
	 * @param task
	 * @return
	 */
	public static DashboardDataItemDrilldownDTO of(Tasks task) {
		String view = TASK;
		Map<String, String> params = new HashMap<>();
		params.put("task", task.getId().toString());
		params.put("taskName", task.getName());

		DashboardDataItemDrilldownDTO result = new DashboardDataItemDrilldownDTO(view, params);

		return result;
	}

	/**
	 * Get Dashboard data drilldown for Project
	 *
	 * @param project
	 * @return
	 */
	public static DashboardDataItemDrilldownDTO of(Projects project) {
		String view = PROJECT;
		Map<String, String> params = new HashMap<>();
		params.put("project", project.getId().toString());
		params.put("projectName", project.getName());

		DashboardDataItemDrilldownDTO result = new DashboardDataItemDrilldownDTO(view, params);

		return result;
	}

	/**
	 * Get Dashboard data drilldown for Assessment
	 *
	 * @param assessment
	 * @return
	 */
	public static DashboardDataItemDrilldownDTO of(Assessments assessment) {
		String view = ASSESSMENT;
		Map<String, String> params = new HashMap<>();
		params.put("assessment", assessment.getId().toString());
		params.put("assessmentName", assessment.getName());

		DashboardDataItemDrilldownDTO result = new DashboardDataItemDrilldownDTO(view, params);

		return result;
	}

	/**
	 * Get Dashboard data drilldown for User
	 *
	 * @param item
	 * @param view
	 * @param metricDomain
	 * @return
	 */
	public static DashboardDataItemDrilldownDTO of(Systems item, String view, MetricDomain metricDomain) {
		Map<String, String> params = new HashMap<>();
		params.put(PARAM_ITEM, item.getId().toString());
		if (metricDomain != null) {
			params.put(PARAM_CATEGORY, metricDomain.getCode());
		}

		DashboardDataItemDrilldownDTO result = new DashboardDataItemDrilldownDTO(view, params);

		return result;
	}

	/**
	 * Get Dashboard data drilldown for User
	 *
	 * @param item
	 * @param view
	 * @param metricDomain
	 * @return
	 */
	public static DashboardDataItemDrilldownDTO of(Users item, String view, MetricDomain metricDomain) {
		Map<String, String> params = new HashMap<>();
		params.put(PARAM_ITEM, item.getId().toString());
		if (metricDomain != null) {
			params.put(PARAM_CATEGORY, metricDomain.getCode());
		}

		DashboardDataItemDrilldownDTO result = new DashboardDataItemDrilldownDTO(view, params);

		return result;
	}

	/**
	 * Get Dashboard data drilldown
	 *
	 * @param view
	 * @param category
	 * @return
	 */
	public static DashboardDataItemDrilldownDTO of(String view, String category) {
		return of(view, category, null);
	}

	/**
	 * Get Dashboard data drilldown
	 *
	 * @param view
	 * @param category
	 * @param item
	 * @return
	 */
	public static DashboardDataItemDrilldownDTO of(String view, String category, String item) {
		Map<String, String> params = new HashMap<>();
		if (item != null) {
			params.put(PARAM_ITEM, item);
		}
		if (category != null) {
			params.put(PARAM_CATEGORY, category);
		}

		DashboardDataItemDrilldownDTO result = new DashboardDataItemDrilldownDTO(view, params);

		return result;
	}

	/**
	 * Get Dashboard data drilldown for System
	 *
	 * @param system
	 * @param metricDomain
	 * @return
	 */
	public static DashboardDataItemDrilldownDTO ofQuant(Systems system, QuantsDomain metricDomain) {
		return ofQuantDomain(system, (metricDomain != null ? metricDomain.getId() : null));
	}

	/**
	 * Get Dashboard data drilldown for System
	 *
	 * @param system
	 * @param metricDomainId
	 * @return
	 */
	public static DashboardDataItemDrilldownDTO ofQuantDomain(Systems system, Long metricDomainId) {
		String view = SYSTEM_QUANTS;
		Map<String, String> params = new HashMap<>();
		params.put("system", system.getId().toString());
		params.put("systemName", system.getName());
		if (metricDomainId != null) {
			view = SYSTEM_QUANTS_DOMAIN;

			params.put("metricDomain", metricDomainId.toString());
		}

		DashboardDataItemDrilldownDTO result = new DashboardDataItemDrilldownDTO(view, params);

		return result;
	}

	/**
	 * Get Dashboard data drilldown for Organization
	 *
	 * @param quantMetrics
	 * @param metricDomainId
	 * @return
	 */
	public static DashboardDataItemDrilldownDTO ofOrganizationQuants(QuantMetrics quantMetrics, Long metricDomainId) {
		String view = ORGANIZATION_QUANTS;
		Map<String, String> params = new HashMap<>();
		if (quantMetrics != null) {
			params.put("metricDomain", quantMetrics.getQuant().getId().toString());
			params.put("metricId", quantMetrics.getId().toString());
		} else if (metricDomainId != null) {
			params.put("metricDomain", metricDomainId.toString());
		}

		DashboardDataItemDrilldownDTO result = new DashboardDataItemDrilldownDTO(view, params);

		return result;
	}

}
