package com.cyberintech.vrisk.server.rest.exception;

/**
 * Application Exception Codes
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-22
 */
public class ApplicationExceptionCodes {
	// Default ERROR codes
	public static final Integer PASSWORD_DOESNT_MATCH = 1141;

	public static final Integer CREATE_IS_NOT_ALLOWED_FOR_ITEM_WITH_EXISTING_ID = 1201;
	public static final Integer UPDATE_IS_NOT_ALLOWED_FOR_ITEM_WITHOUT_ID = 1202;

	// User ERRORs range: 1300-1399
	public static final Integer USER_WITH_EMAIL_ALREADY_EXISTS = 1301;
	public static final Integer USER_NOT_EXISTS = 1302;
	public static final Integer USER_NOT_BELONGS_TO_CURRENT_ORGANIZATION = 1303;
	public static final Integer USER_WITH_EMAIL_NOT_EXISTS = 1304;
	public static final Integer USER_MANAGEMENT_FORBIDDEN = 1313;
	public static final Integer USER_REGISTRATION_EMAIL_FAILED = 1314;
	public static final Integer RESET_PASSWORD_LINK_EMAIL_FAILED = 1323;
	public static final Integer RESET_PASSWORD_LINK_NOT_EXISTS = 1324;
	public static final Integer RESET_PASSWORD_LINK_EXPIRED = 1325;
	public static final Integer RESET_PASSWORD_LINK_ALREADY_APPLIED = 1326;
	public static final Integer RESET_PASSWORD_TOO_WEAK = 1327;

	// Organization ERRORs range: 1400-1499
	public static final Integer ORGANIZATION_WITH_NAME_ALREADY_EXISTS = 1401;
	public static final Integer ORGANIZATION_NOT_EXISTS = 1402;
	public static final Integer ACCESS_TO_ORGANIZATION_FORBIDDEN = 1403;
	public static final Integer ONLY_VENDOR_ORGANIZATION_CAN_BE_UPDATED = 1411;
	public static final Integer ONLY_SUBSIDIARY_ORGANIZATION_CAN_BE_UPDATED = 1431;
	public static final Integer ONLY_SUBSIDIARY_ORGANIZATION_CAN_BE_REMOVED = 1433;
	public static final Integer ORGANIZATION_CANNOT_BE_REMOVED = 1435;
	public static final Integer VENDOR_REQUIRED = 1421;
	public static final Integer VENDOR_NOT_EXISTS = 1422;
	public static final Integer VENDOR_CANNOT_BE_REMOVED = 1425;
	public static final Integer ORGANIZATION_ID_REQUIRED = 1431;
	public static final Integer RISK_MODEL_ID_REQUIRED = 1432;

	// User ERRORs range: 4200-4299
	public static final Integer METRIC_DOMAIN_NOT_EXISTS = 4201;
	public static final Integer METRIC_DOMAIN_CODE_NOT_EXISTS = 4202;

	// User ERRORs range: 4400-4409
	public static final Integer METRIC_RISK_NOT_EXISTS = 4401;

	// Technology Category ERRORs range: 4410-4419
	public static final Integer TECHNOLOGY_CATEGORY_NOT_EXISTS = 4414;
	public static final Integer TECHNOLOGY_CATEGORY_ALREADY_EXISTS = 4415;
	public static final Integer TECHNOLOGY_CATEGORY_FIRST_CHARACTER = 4416;
	public static final Integer TECHNOLOGY_CATEGORY_EMPTY = 4417;

	// Technology ERRORs range: 4420-4429
	public static final Integer TECHNOLOGY_NOT_EXISTS = 4424;

	// Systems ERRORs range: 4460-4469
	public static final Integer SYSTEM_NOT_EXISTS = 4464;
	public static final Integer SYSTEM_REQUIRED = 4465;

	// Processs ERRORs range: 4470-4479
	public static final Integer PROCESS_NOT_EXISTS = 4474;

	// Assessment ERRORs range: 4510-4519
	public static final Integer ASSESSMENT_NOT_EXISTS = 4514;
	public static final Integer ASSESSMENT_IMPORT_FORBIDDEN = 4515;

	// Assessment Types ERRORs range: 4520-4529
	public static final Integer ASSESSMENT_TYPE_NOT_EXISTS = 4524;

	// Assessment Levels ERRORs range: 4530-4539
	public static final Integer ASSESSMENT_LEVEL_NOT_EXISTS = 4534;

	// Assessment Weight ERRORs range: 4540-4549
	public static final Integer ASSESSMENT_WEIGHT_NOT_EXISTS = 4544;

	// Control Function ERRORs range: 4620-4629
	public static final Integer CONTROL_FUNCTION_NOT_EXISTS = 4624;

	// Control Category ERRORs range: 4630-4639
	public static final Integer CONTROL_CATEGORY_NOT_EXISTS = 4634;

	// Control Subcategory ERRORs range: 4640-4649
	public static final Integer CONTROL_SUBCATEGORY_NOT_EXISTS = 4644;

	// GDPR ERRORs range: 4710-4740
	public static final Integer GDPR_IMPORT_FORBIDDEN = 4710;

	// Policy ERRORs range: 4741-4749
	public static final Integer POLICY_NOT_EXIST = 4741;

	// Organization Agreement ERRORs range: 4750-4759
	public static final Integer ORGANIZATION_AGREEMENT_NOT_EXIST = 4750;

	// User Agreement ERRORs range: 4760-4769
	public static final Integer USER_AGREEMENT_NOT_EXIST = 4760;

	// Security Control Families ERRORs range: 4770 - 4779
	public static final Integer SECURITY_CONTROL_FAMILY_NOT_EXISTS = 4770;

	// Security Control Names ERRORs range: 4780 - 4789
	public static final Integer SECURITY_CONTROL_NAME_NOT_EXISTS = 4780;

	// Security Requirement ERRORs range: 4790 - 4799
	public static final Integer SECURITY_REQUIREMENT_REQUIRED = 4790;
	public static final Integer SECURITY_REQUIREMENT_IMPORT_FORBIDDEN = 4791;

	// Assigned items errors
	public static final Integer ASSIGNED_USER_REQUIRED = 4801;
	public static final Integer ASSIGNED_SYSTEM_REQUIRED = 4802;
	public static final Integer ASSIGNED_VENDOR_REQUIRED = 4803;
	public static final Integer ASSIGNED_USER_NOT_EXISTS = 4804;
	public static final Integer ASSIGNED_SYSTEM_NOT_EXISTS = 4805;
	public static final Integer ASSIGNED_VENDOR_NOT_EXISTS = 4806;
	public static final Integer ASSIGNED_SYSTEM_EMAIL_FAILED = 4823;
	public static final Integer ASSIGNED_VENDOR_EMAIL_FAILED = 4824;

	// Formulas ERRORs range: 4850 - 4859
	public static final Integer FORMULA_NOT_EXIST = 4850;

	// Vulnerabilities ERRORs range: 4860 - 4869
	public static final Integer VULNERABILITY_NOT_EXIST = 4860;

	// Supported Languages ERRORs range: 4870 - 4879
	public static final Integer SUPPORTED_LANGUAGE_NOT_EXIST = 4870;

	// Hint ERRORs range: 4910-4919
	public static final Integer HINT_NOT_EXISTS = 4914;
	public static final Integer HINT_ALREADY_EXISTS = 4915;
	public static final Integer HINT_FIRST_CHARACTER = 4916;
	public static final Integer HINT_CODE_EMPTY = 4917;

	// Audit Logs ERRORs range: 5001-5020
	public static final Integer AUDIT_LOGS_MISSING_ITEM_ID_IN_SEARCH = 5011;

	// Business Units ERRORs range: 5021 - 5100
	public static final Integer BUSINESS_UNIT_HAS_CHILDS = 5011;

	// Feeds ERRORs range 5101 - 5120

	public static final Integer FEEDS_MANAGEMENT_FORBIDDEN = 5101;

	// Parse ERRORs range 5121 - 5140
	public static final Integer UNPARSEABLE_DATE = 5121;
	public static final Integer EMPTY_FORMULA = 5122;

	public static final Integer INDUSTRY_ALREADY_EXISTS = 5215;
	public static final Integer INDUSTRY_HAS_CHILDREN = 5216;
	public static final Integer INDUSTRY_HAS_LINKS = 5217;

}
