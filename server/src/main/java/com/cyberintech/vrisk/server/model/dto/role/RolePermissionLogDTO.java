package com.cyberintech.vrisk.server.model.dto.role;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

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
public class RolePermissionLogDTO {

	private PermissionRefDTO permission;
	private RoleListDTO role;
	private boolean isAllowed;
	private String name;

	/**
	 * Default constructor
	 */
	public RolePermissionLogDTO() {
	}

	public static RolePermissionLogDTO of(PermissionRefDTO permission, RoleListDTO role, boolean isAllowed) {
		RolePermissionLogDTO result = new RolePermissionLogDTO();
		result.setPermission(permission);
		result.setRole(role);
		result.setAllowed(isAllowed);
		String name = "";
		if (permission != null) name += permission.getName();
		if (role != null) name += (StringUtils.isNotEmpty(name) ? " / " : "") + role.getName();

		result.setName(name);

		return result;
	}

}
