package com.cyberintech.vrisk.server.model.jpa.domains;

import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Predefined Audit Operation Item Types
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-07-23
 */
@Getter
@ToString(of = {"id", "name"})
public enum AuditOperationType {

	UNKNOWN(0L, "Unknown")
	, CREATE(1L, "Create")
	, UPDATE(2L, "Update")
	, DELETE(3L, "Delete")
	, EVENT(11L, "Event")
	;

	private final Long id;

	private final String name;

	public static Map<Long, AuditOperationType> ALL_ITEMS_MAP = Arrays.stream(AuditOperationType.values()).collect(Collectors.toMap(AuditOperationType::getId, itemType -> itemType));

	private AuditOperationType(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Returns Type Entity By ID
	 *
	 * @param id
	 * @return
	 */
	public static AuditOperationType of(Long id) {

		if (id != null && ALL_ITEMS_MAP.containsKey(id)) {
			return ALL_ITEMS_MAP.get(id);
		}

		return UNKNOWN;
	}

}
