package com.cyberintech.vrisk.server.model.dto.audit;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.domains.AuditOperationType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLog;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;

/**
 * Audit Log Related Operation
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-06-28
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class AuditLogRelatedOperationDTO extends DTOBase<AuditLogItemId> {

	@Schema
	private VItemType itemType;

	@Schema
	private Long itemId;

	@Schema
	private String value;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public AuditLogRelatedOperationDTO(AuditLogItemId entity) {
		super(entity);
	}

	/**
	 * Converts from entity to DTO
	 *
	 * @param entity
	 */
	@Override
	public void fromEntity(AuditLogItemId entity) {
		this.itemId = entity.getItemId();
		this.value = entity.getValue();
		this.itemType = VItemType.of(entity.getItemType());
	}

}
