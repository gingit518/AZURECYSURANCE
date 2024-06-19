package com.cyberintech.vrisk.server.model.dto.audit;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.domains.AuditOperationType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLog;
import lombok.*;

import java.util.Date;

/**
 * Audit Log View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-07-25
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class AuditLogViewDTO extends DTOBase<AuditLog> {

	private Long id;

	private ItemTypeDTO itemTypeInfo;

	private AuditOperationType operationTypeInfo;

	private String oldValue;

	private String newValue;

	private Date logDate;

	private Long auditItemId;

	private Long auditUserId;

	private String auditUserName;

	private String auditUserEmail;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public AuditLogViewDTO(AuditLog entity) {
		super(entity);
	}

	/**
	 * Converts from entity to DTO
	 *
	 * @param entity
	 */
	@Override
	public void fromEntity(AuditLog entity) {
		super.fromEntity(entity);

		itemTypeInfo = new ItemTypeDTO(VItemType.of(entity.getItemType()));
		operationTypeInfo = AuditOperationType.of(entity.getOperationType());
	}

}
