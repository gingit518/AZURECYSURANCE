package com.cyberintech.vrisk.server.model.dto.user;

import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-10-27
 */
@Setter
@Getter
@NoArgsConstructor
public class ExtendedUserListDTO extends UserListDTO {

	@Schema
	private ItemViewDTO<Organizations> organization;

	/**
	 * Entity based constructor
	 *
	 * @param users
	 */
	public ExtendedUserListDTO(Users users) {
		super(users);
	}

	/**
	 * Converts from entity to DTO
	 *
	 * @param entity
	 */
	@Override
	public void fromEntity(Users entity) {
		super.fromEntity(entity);

		if (entity.getOrganization() != null) setOrganization(new ItemViewDTO<Organizations>(entity.getOrganization()));
	}

}
