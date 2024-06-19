package com.cyberintech.vrisk.server.model.dto.role;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Permission and Role Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-19
 */
@Setter
@Getter
@ToString(of = {"permission", "role"})
@EqualsAndHashCode(of = {"permission", "role"}, callSuper = false)
public class RolePermissionUpdateDTO {

	private PermissionRefDTO permission;
	private RoleListDTO role;
	private boolean isAllowed;

	/**
	 * Default constructor
	 */
	public RolePermissionUpdateDTO() {
	}

	public static RolePermissionUpdateDTO of(PermissionRefDTO permission, RoleListDTO role, boolean isAllowed) {
		RolePermissionUpdateDTO result = new RolePermissionUpdateDTO();
		result.setPermission(permission);
		result.setRole(role);
		result.setAllowed(isAllowed);

		return result;
	}

}
