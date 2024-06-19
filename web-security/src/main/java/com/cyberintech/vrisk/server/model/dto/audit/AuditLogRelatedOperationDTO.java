package com.cyberintech.vrisk.server.model.dto.audit;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import lombok.*;

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

	private VItemType itemType;

	private Long itemId;

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
