package com.cyberintech.vrisk.server.model.jpa.domains;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Predefined VRisk Document Types
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-10-16
 */
@Getter
@Schema
public enum DocumentType {

	DEFAULT(0L, "Default", "default")
	, PUBLIC(1L, "Public Documents", "public")
	, PRIVATE(2L, "Private Documents", "private")
	, IMAGE(4L, "Public Image Resources", "image")
	, IMAGE_PRIVATE(5L, "Private Image Resources", "image_private")
	, GDPR(8L, "GDPR", "gdpr")
	, CONTROL_TEST_EVIDENCE(9L, "CONTROL_TEST_EVIDENCE", "control_test_evidence")
	, QUESTION_ANSWER(16L, "Question Answer", "question_answer")
	, POLICY(20L, "Policy", "policy")
	, VENDOR_CONTRACT(22L, "VENDOR_CONTRACT", "vendor_contract")
	, REGULATION_DOCUMENT(134L, "REGULATION_DOCUMENT", "regulations")
	;

	private final Long id;

	private final String typeName;

	private final String remotePath;

	public static Map<Long, DocumentType> ALL_ITEMS_MAP = Arrays.stream(DocumentType.values()).collect(Collectors.toMap(DocumentType::getId, itemType -> itemType));

	private DocumentType(Long id, String typeName, String remotePath) {
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
	public static DocumentType of(Long id) {

		if (id != null && ALL_ITEMS_MAP.containsKey(id)) {
			return ALL_ITEMS_MAP.get(id);
		}

		return DEFAULT;
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
