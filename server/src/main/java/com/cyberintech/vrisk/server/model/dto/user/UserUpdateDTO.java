package com.cyberintech.vrisk.server.model.dto.user;

import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.UserEmploymentType;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Roles;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.service.UserService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-10-17
 */
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"}, callSuper = true)
public class UserUpdateDTO extends UserCreateDTO {

	@Schema
	private Long id;

	@Schema
	private DocumentDTO profilePicture;

	@Schema
	private Boolean removeProfilePicture;

	public UserUpdateDTO() {
		super();
	}

	public UserUpdateDTO(Users entity) {
		super(entity);
	}


	/**
	 * Converts from entity to DTO
	 *
	 * @param entity
	 */
	@Override
	public void fromEntity(Users entity) {
		super.fromEntity(entity);

		if (entity.getProfilePicture() != null) {
			this.setProfilePicture(new DocumentDTO(entity.getProfilePicture()));
		}
	}

}
