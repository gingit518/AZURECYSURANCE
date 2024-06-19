package com.cyberintech.vrisk.server.model.jpa.domains;

import lombok.Getter;
import lombok.ToString;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Predefined VRisk Item Types
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-07-23
 */
@Getter
@ToString(of = {"id", "name", "itemType"})
public enum VItemType {

	UNKNOWN(0L, "Unknown")
	, ASSET_CLASS(7L, "Asset Class") // DONE
	, BUSINESS_UNIT(20L, "Business Unit") // DONE
	, BUSINESS_UNIT_OWNER(21L, "Business Unit Owner")
	, DATA_TYPE_CLASS(31L, "Data Type Class") // DONE
	, DATA_DOMAIN(32L, "Data Domain") // DONE
	, DATA_FIELD(33L, "Data Field") // DONE
	, ENVIRONMENT_TYPE(34L, "Environment Type") // DONE
	, OWNER_USER(45L, "Owner User for Item")
	, PROCESS(47L, "Process")
	, SYSTEM(51L, "System") // DONE
	, SYSTEM_OWNER(52L, "System Owner") // DONE
	, RISK_MODEL_DOMAIN(72L, "Risk Model Domain") // DONE
	, CATEGORY_DOMAIN(73L, "Category Domain") // DONE
	, ASSOCIATE_MODELS(76L, "Associate Models") // DONE
	, ORGANIZATION(91L, "Organization")
	, PROJECT(921L, "Project")
	, QUANTIFICATION_METRIC(94L, "Quantification Metric")
	, QUALITATIVE_METRIC_DOMAIN(96L, "Qualitative Metric Domain")
	, QUALITATIVE_METRIC(97L, "Qualitative Metric")
	, QUALITATIVE_QUESTION(101L, "Qualitative Question")
	, QUALITATIVE_QUESTION_ANSWERS(102L, "Question Answer")
	, QUALITATIVE_QUESTION_ANSWER_FOR_SYSTEM(103L, "System Answer") // DONE
	, QUALITATIVE_QUESTION_ANSWER_FOR_VENDOR(104L, "Vendor Answer") // DONE
	, RISK_MODEL(106L, "Risk Model")
	, SUBSIDIARY_ORGANIZATION(110L, "Subsidiary Organization")
	, TASK_CATEGORY(1011L, "Task Category")
	, TASK(1012L, "Task")
	, TECHNOLOGY_CATEGORY(114L, "Technology Category")
	, TECHNOLOGY(115L, "Technology")
	, USER(117L, "User") // DONE
	, USER_PASSWORD_CHANGED(118L, "Password Changed") // DONE
	, USER_PASSWORD_LOGIN(1191L, "User Password Login") // DONE
	, USER_SMS_CODE_LOGIN(1192L, "User 2FA SMS Code Login") // DONE
	, USER_SMS_CODE_WRONG(1193L, "User 2FA Code Wrong") // DONE
	, USER_SMS_CODE_FAILED(1194L, "User 2FA Code Failed") // DONE
	, USER_OTP_CODE_LOGIN(11921L, "User 2FA OTP Code Login") // DONE
	, USER_OTP_CODE_WRONG(11931L, "User 2FA OTP Code Wrong") // DONE
	, USER_OTP_CODE_FAILED(11941L, "User 2FA OTP Code Failed") // DONE
	, USER_GOOGLE_LOGIN(1195L, "User Google Login")
	, USER_MICROSOFT_LOGIN(1196L, "User Microsoft Login")
	, USER_OKTA_LOGIN(11961L, "User OKTA Login")
	, USER_LOGOUT(1198L, "User Logout") // DONE
	, VENDOR(121L, "Vendor") // DONE
	, VENDOR_OWNER(122L, "Vendor Owner") // DONE
	, VENDOR_ASSOCIATED_SYSTEMS(123L, "Associate Systems for Vendor") // DONE
	, DOCUMENT_FILE(131L, "Document FIle") // DONE
	, GDPR_ARTICLE(263L, "GDPR Article") // DONE
	, GDPR_ORGANIZATION_ARTICLE_STATUS(275L, "GDPR Organization Article Status") // DONE
	, GDPR_ORGANIZATION_STATUS(276L, "GDPR Organization Status") // DONE
	, GDPR_SYSTEM_STATUS(277L, "GDPR System Status") // DONE
	, GDPR_SYSTEM_ARTICLE_STATUS(278L, "GDPR System Article Status") // DONE
	, GDPR_EVIDENCE_DOCUMENTS(279L, "GDPR Evidence Documents") // DONE
	, POLICY(280L, "Policy")
	, ORGANIZATION_AGREEMENT(281L, "Organization Agreement")
	, USER_AGREEMENT(282L, "User Agreement")
	, SECURITY_REQUIREMENT(283L, "Security Requirement")
	, ORGANIZATION_REQUIREMENT_CONTROL_TEST_RESULT(284L, "Organization Requirement Control Test Result")
	, SYSTEM_REQUIREMENT_CONTROL_TEST_RESULT(285L, "System Requirement Control Test Result")
	, SYSTEM_CONTROL_TEST_RESULT(286L, "System Control Test Result")
	, FORMULA(287L, "Formula")
	, RISK_METRIC(288L, "Risk Metric")
	, VULNERABILITY(289L, "Vulnerability")
	, REGULATION(290L, "Regulation")
	, EXTERNAL_ANALYTICS(291L, "External Analytics")
	, SECURITY_AUDIT_COMMENT(385L, "Security Audit Comment"), RISK_MODEL_CONSTANT(386L, "Risk Model Constant"), HINT(387L, "Hints"), MENU_ITEM(388L, "Menu Items")

	// Zoom Info integrations
	, ZOOM_INFO_ORGANIZATION_SYNC(1401L, "Zoom Info Organization Sync"), ZOOM_INFO_TECHNOLOGY_CATEGORY_SYNC(1402L, "Zoom Info Technology Category Sync"), ZOOM_INFO_TECHNOLOGY_SYNC(1403L, "Zoom Info Technology Sync"), ZOOM_INFO_SYSTEM_SYNC(1404L, "Zoom Info System Sync"), ZOOM_INFO_VENDOR_SYNC(1405L, "Zoom Info Vendor Sync"), ZOOM_INFO_BUSINESS_UNIT_SYNC(1406L, "Zoom Info Business Unit Sync"), ZOOM_INFO_SUBSIDIARY_ORGANIZATION_SYNC(1407L, "Zoom Info Subsidiary Organization Sync")

	// big id integrations
	, BIG_ID_SYSTEM_SYNC(2000L, "Big Id System Sync"), BIG_ID_USER_SYNC(2001L, "Big Id User sync"), BIG_ID_TECHNOLOGY_CATEGORY_SYNC(2002L, "Big Id Technology Category Sync"), BIG_ID_TECHNOLOGY_SYNC(2003L, "Big Id Technology Sync"), BIG_ID_DATA_TYPE_CLASSIFICATION_SYNC(2004L, "Big Id Data Type Classification Sync"), BIG_ID_DATA_ASSET_CLASSIFICATION_SYNC(2005L, "Big Id Data Asset Classification Sync"), BIG_ID_ROOT_ORGANIZATION_SYNC(2006L, "Big Id Root Organization Sync"), BIG_ID_SUB_ORGANIZATION_SYNC(2007L, "Big Id Sub Organization Sync");
	private final Long id;

	private final String name;

	private final String itemType;

	public static Map<Long, VItemType> ALL_ITEMS_MAP = Arrays.stream(VItemType.values()).collect(Collectors.toMap(VItemType::getId, itemType -> itemType));

	private VItemType(Long id, String name) {
		this.id = id;
		this.name = name;
		this.itemType = name();
	}

	/**
	 * Returns Type Entity By ID
	 *
	 * @param id
	 * @return
	 */
	public static VItemType of(Long id) {

		if (id != null && ALL_ITEMS_MAP.containsKey(id)) {
			return ALL_ITEMS_MAP.get(id);
		}

		return UNKNOWN;
	}

}
