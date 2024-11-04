package com.cyberintech.vrisk.server.model.dto.organization;

import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Vendor Reference Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2024-10-08
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class VendorRefDTO extends OrganizationRefDTO {

	@Schema
	private UserRefDTO owner;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public VendorRefDTO(Organizations entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Organizations entity) {
		super.fromEntity(entity);

		if (entity.getOwner() != null) {
			this.owner = new UserRefDTO(entity.getOwner());
		}
	}
}
