package com.cyberintech.vrisk.server.model.dto.audit.items;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Vendor View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-08-06
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class VendorOwnerAuditDTO extends DTOBase<Organizations> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private UserRefDTO owner;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public VendorOwnerAuditDTO(Organizations entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Organizations entity) {
		super.fromEntity(entity);

		if (entity.getOwner() != null) {
			owner = new UserRefDTO(entity.getOwner());
		}
	}
}
