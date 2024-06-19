package com.cyberintech.vrisk.server.model.dto.user;

import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Roles;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.service.UserService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
public class UserListAdminDTO extends UserListDTO {

	private OrganizationRefDTO organization;

	private Boolean deleted;

	@Schema
	private List<OrganizationRefDTO> vendors;

	/**
	 * User role codes list
	 */
	@Schema(type = "array", name = "List of User role objects")
	private List<ItemViewDTO<Roles>> roles;

	/**
	 * Entity based constructor
	 *
	 * @param users
	 */
	public UserListAdminDTO(Users users) {
		super(users);
	}

	/**
	 * Initialize current DTO object from Users entity
	 *
	 * @param entity
	 */
	@Override
	public void fromEntity(Users entity) {
		super.fromEntity(entity);

		deleted = entity.getDeleted();

		if (entity.getOrganization() != null) {
			organization = new OrganizationRefDTO(entity.getOrganization());
		}

		if (UserService.isVendorEmployee(entity)) {
			vendors = Optional.ofNullable(entity.getVendors()).orElse(new HashSet<>()).stream().map(OrganizationRefDTO::new).collect(Collectors.toList());;
		}

		roles = Optional.ofNullable(entity.getRoles()).orElse(new HashSet<>()).stream().map(roles1 -> new ItemViewDTO<Roles>(roles1)).collect(Collectors.toList());
	}
}
