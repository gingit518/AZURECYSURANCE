package com.cyberintech.vrisk.server.model.jpa.domains;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Basic Role Types
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-08
 */
public enum RoleType {

	/**
	 * Default system user
	 */
	USER("USER"),

	/**
	 * System Administrator
	 */
	ADMIN("ADMIN"),

	/**
	 * Chief Executive Officer (CEO) responsible to increase the value of the company.
	 */
	CHIEF_EXECUTIVE_OFFICER("CEO"),

	/**
	 * Chief Risk Officer (CRO) is responsible for the enterprise risk of the company including cyber risk management.
	 */
	CHIEF_RISK_OFFICER("CRO"),

	/**
	 * Chief Information Security Officer (CISO).
	 * A CISO is a senior-level executive within an organization responsible for establishing and maintaining the enterprise vision, strategy, and cyber security program to ensure digital assets are adequately protected.
	 */
	CHIEF_INFORMATION_SECURITY_OFFICER("CISO"),

	/**
	 * Compliance Manager. A compliance manager or officer is an employee whose responsibilities include ensuring the company complies with its outside regulatory requirements and internal policies.
	 */
	COMPLIANCE_MANAGER("CLM"),

	/**
	 * The DPO must ensure that the organization complies with GDPR regulation if it processes EU citizen privacy data regardless of where it is located.
	 * The DPO must have a deep knowledge of the GDPR and an awareness where possible regulatory breaches may occur.
	 */
	DATA_PROTECTION_OFFICER("DPO"),

	/**
	 * Remediator. Fixes vulnerabitlities, incidents and cyber security findings.
	 */
	REMEDIATOR("REM"),

	/**
	 * System Owner. Is repsonsible for all decisions regarding the use and maintenance of the system.
	 */
	SYSTEM_OWNER("SYSOWN"),

	/**
	 * InfoSec Focal Person. Is the point person that works with a system or process from the information security team.
	 */
	INFORMATION_SECURITY("INFOSEC"),

	/**
	 * Task Manager. Owns a specific task or activity in the organization.
	 */
	TASK_MANAGER("TSM"),

	/**
	 * Project Manager. Manages a specific project in an organization.
	 */
	PROJECT_MANAGER("PM"),

	/**
	 * Business Unit Owner. Is responsible for all decisions regarding the use and maintenance of the business unit.
	 */
	BUSINESS_UNIT_OWNER("BUO"),

	/**
	 * Auditor. IT Auditors are responsible for developing, planning, and executing IT audit programs based on risk assessments in a highly integrated audit environment.
	 */
	AUDITOR("AUD"),

	/**
	 * Risk Manager. Responsible to create the risk model. Manages risk of a specific nature.
	 */
	RISK_MANAGER("RISKMAN"),

	/**
	 * Process Owner. Is responsible for all decisions regarding the use and maintenance of the process.
	 */
	PROCESS_OWNER("PRCOWN"),

	/**
	 * Vendor Owner. Is responsible for all decisions regarding the use and maintenance of the vendor.
	 */
	VENDOR_OWNER("VNDOWN"),

	/**
	 * Organization Admin. May handle any operations inside organization.
	 */
	ORGANIZATION_ADMIN("ORGADMIN"),

	/**
	 * Enterprise Risk Manager. Manages all the systems. Risk Manager only a certain system.
	 */
	ENTERPRISE_RISK_MANAGER("ERM"),

	/**
	 * Technical System Owner answers the questions that the system owner cannot. For example: # of records.
	 */
	TECHNICAL_SYSTEM_OWNER("TSO")

	/**
	 * Employee of the Vendor who is communicate with Organization to ask/answer the questions.
	 */
	, VENDOR_EMPLOYEE("VENDOR")

	/**
	 * Admin of the Vendor. Can setup Vendor account details.
	 */
	, VENDOR_ADMIN_EXTERNAL("VNDSUP")

	/**
	 * External Vendor Admin who is going to communicate with Organization to ask/answer the questions. This user is allowed to create external vendor account to insert proper data.
	 */
	, VENDOR_ADMIN("VENDOR_ADMIN")

	/**
	 * Technology IT Owner
	 */
	, TECH_IT_OWNER("TECITOWN")

	/**
	 * Technology Business Owner
	 */
	, TECH_BUSINESS_OWNER("TECBSOWN")

	/**
	 * Data Type Class Owner
	 */
	, DATA_TYPE_CLASS_OWNER("DTCLSOWN")

	/**
	 * Chief Financial Officer. Typically, oversees cyber risk and insurance.
	 */
	, CHIEF_FINANCIAL_OFFICER("CFO")

	, BUS_CONT_MANAGER("BUS_CONT_MANAGER")
	, CCPA("CCPA")
	, CCPA_ADMIN("CCPA_ADMIN")
	, ORG_READ("ORG_READ")
	, ELASTIO_ADMIN("ELASTIO_ADMIN")
	, ELASTIO("ELASTIO")

	;

	private final String _role;

	public static Map<String, RoleType> ALL_ROLES_MAP = Arrays.stream(RoleType.values()).collect(Collectors.toMap(RoleType::role, itemType -> itemType));

	private RoleType(String role) {
		this._role = role;
	}

	@Override
	public String toString() {
		return this.role();
	}

	/**
	 * Get Role Code Definition
	 *
	 * @return
	 */
	public String role() {
		return _role;
	}

	/**
	 * Checks is role defined as system
	 *
	 * @param name
	 * @return
	 */
	public static boolean isRoleDefined(String name) {
		return ALL_ROLES_MAP.containsKey(name);
	}

}
