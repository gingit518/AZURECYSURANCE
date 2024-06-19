package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Audit Log Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-07-23
 */
@Entity
@Table(name = "audit_logs")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "organizationId", "newValue"})
@EqualsAndHashCode(of = {"id"})
public class AuditLog {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@Column(name = "operation_type")
	private Long operationType;

	@Column(name = "item_type")
	private Long itemType;

	@Column(name = "old_value")
	private String oldValue;

	@Column(name = "new_value")
	private String newValue;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "log_date")
	private Date logDate;

	@Column(name = "audit_item_id")
	private Long auditItemId;

	@Column(name = "audit_user_id")
	private Long auditUserId;

	@Column(name = "audit_user_name")
	private String auditUserName;

	@Column(name = "audit_user_email")
	private String auditUserEmail;

	@OneToMany(cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
	@JoinColumn(name = "audit_log_id")
	private Set<AuditLogItemId> auditLogItemIds = new HashSet<>();

}
