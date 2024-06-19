package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Audit Log Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-07-23
 */
@Entity
@Table(name = "audit_log_item_ids")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "organizationId", "value"})
@EqualsAndHashCode(of = {"id"})
public class AuditLogItemId {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@Column(name = "item_type")
	private Long itemType;

	@Column(name = "item_id")
	private Long itemId;

	@Column(name = "value")
	private String value;

	@Transient
	public static AuditLogItemId of(VItemType itemType, Long itemId, String value) {
		AuditLogItemId result = new AuditLogItemId();
		result.setItemType(itemType.getId());
		result.setItemId(itemId);
		result.setValue(value);

		return result;
	}

	@Transient
	public static AuditLogItemId of(VItemType itemType, Long itemId) {
		return of(itemType, itemId, null);
	}

}
