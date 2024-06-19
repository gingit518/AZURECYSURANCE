package com.cyberintech.vrisk.server.model.dto.audit;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.domains.AuditOperationType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLog;
import lombok.*;

import java.util.Date;

/**
 * Audit log Item Type View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-07-25
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name", "itemType"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class ItemTypeDTO {

	private Long id;

	private VItemType itemType;

	private String name;

	/**
	 * Entity based constructor
	 */
	public ItemTypeDTO(VItemType itemType) {
		if (itemType != null) {
			this.id = itemType.getId();
			this.name = itemType.getName();
			this.itemType = itemType;
		}
	}

}
