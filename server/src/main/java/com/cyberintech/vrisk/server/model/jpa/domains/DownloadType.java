package com.cyberintech.vrisk.server.model.jpa.domains;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Predefined VRisk Download Types
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-10-16
 */
@Getter
@Schema
public enum DownloadType {

	BUSINESS_UNITS(1L, "Business Units", "/api/data-export/csv/business-units/export")
	, QUALITATIVE_QUESTIONS(2L, "Qualitative Questions", "/api/data-export/csv/qual-questions/export")
	, QUALITATIVE_QUESTIONS_FOR_TYPE(2002L, "Qualitative Questions for Type", "/api/data-export/csv/qual-questions/export/{questionType}")
	, QUALITATIVE_QUESTION_ANSWERS(3L, "Qualitative Questions Answers", "/api/data-export/csv/qual-questions/answers/export/{itemId}")
	, SCORING_QUESTION_ANSWERS(3001L, "Scoring Questions Answers", "/api/data-export/csv/qual-questions/answers/export")
	, SCORING_QUESTIONS_XLSX_REPORT(3022L, "Scoring Questions XLSX Report", "/api/data-export/xlsx/qual-questions/organization/report/{itemId}")
	, CONTROL_GUIDELINES(4L, "Control Guidelines", "/api/data-export/csv/control-guidlines/export")
	, GDPR_ARTICLES(5L, "GDPR Articles", "/api/data-export/csv/gdpr-articles/export")
	, ASSESSMENT_FRAMEWORKS(6L, "Assessment Frameworks", "/api/data-export/csv/assessment-frameworks/export/{itemId}")
	, SECURITY_REQUIREMENTS(7L, "Security Requirements", "/api/data-export/csv/security-requirements/export")
	, USERS(8L, "Users", "/api/data-export/csv/users/export")
	, SYSTEM_RISKS(9L, "System Risks", "/api/data-export/csv/systems-risk-data/export")
	, VENDORS(10L, "Vendors", "/api/data-export/csv/vendors/export")
	, SUBSIDIARIES(11L, "Subsidiaries", "/api/data-export/csv/subsidiaries/export")
	, PROCESSES(12L, "Processes", "/api/data-export/csv/processes/export")
	, PROCESSES_TEMPLATE(13L, "Processes Template", "/api/data-export/csv/processes/download-template")
	, TECHNOLOGIES(14L, "Technologies", "/api/data-export/csv/technologies/export")
	, TECHNOLOGIES_TEMPLATE(15L, "Technologies Template", "/api/data-export/csv/technologies/download-template")
	, BUSINESS_UNITS_TEMPLATE(22L, "Business Units Template", "/api/data-export/csv/business-unit-template/download")

	, ADMIN_HINTS(101L, "Application Hints", "/api/hints/csv/export")
	, ADMIN_LANGUAGE_CONSTANTS(102L, "Application Language Constants", "/api/admin/language-constants/csv/export/{languageCode}")
	, ADMIN_PERMISSIONS(103L, "Admin Permissions", "/api/admin/permissions/csv/export")
	, ADMIN_NEWS(1011L, "Admin News", "/api/admin/feeds/news/csv/export")
	, ADMIN_PACE_COURSES(1012L, "Admin Pace Courses", "/api/admin/feeds/pace-courses/csv/export")
	, ADMIN_WEBINARS(1013L, "Admin Webinars", "/api/admin/feeds/webinars/csv/export")
	, ADMIN_WHATS_NEW(1014L, "Admin Whats New", "/api/admin/feeds/whats-new/csv/export")

	, QUANT_METRIC(1100L, "Quantification Metric", "/api/data-export/csv/quant-metrics/export")
	;

	private final Long id;

	private final String typeName;

	private final String remotePath;

	public static Map<Long, DownloadType> ALL_ITEMS_MAP = Arrays.stream(DownloadType.values()).collect(Collectors.toMap(DownloadType::getId, itemType -> itemType));

	public static Map<String, DownloadType> ALL_ITEM_NAMES_MAP = Arrays.stream(DownloadType.values()).collect(Collectors.toMap(DownloadType::name, itemType -> itemType));

	private DownloadType(Long id, String typeName, String remotePath) {
		this.id = id;
		this.typeName = typeName;
		this.remotePath = remotePath;
	}

	/**
	 * Returns Type Entity By ID
	 *
	 * @param id
	 * @return
	 */
	public static DownloadType of(Long id) {

		if (id != null && ALL_ITEMS_MAP.containsKey(id)) {
			return ALL_ITEMS_MAP.get(id);
		}

		return null;
	}

	/**
	 * Returns Type Entity By Name
	 *
	 * @param name
	 * @return
	 */
	public static DownloadType ofString(String name) {

		if (name != null && ALL_ITEM_NAMES_MAP.containsKey(name)) {
			return ALL_ITEM_NAMES_MAP.get(name);
		}

		return null;
	}

	/**
	 * Overriding to String value
	 *
	 * @return
	 */
	public String toString() {
		return this.name();
	}

}
