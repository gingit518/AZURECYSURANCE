package com.cyberintech.vrisk.server.model.dto.role;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.Permissions;
import com.cyberintech.vrisk.server.model.jpa.entity.Roles;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import java.util.ArrayList;
import java.util.List;

/**
 * Permission with assigned Roles Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-19
 */
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class PermissionRolesDTO extends DTOBase<Permissions> {

	private Long id;
	private String name;
	private String description;
	private String permissionGroup;
	private String title;
	private String url;
	private Long itemOrder;
	private List<RoleListDTO> roles = new ArrayList<>();

	/**
	 * Default constructor
	 */
	public PermissionRolesDTO() {
		super();
	}

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public PermissionRolesDTO(Permissions entity) {
		super(entity);
	}

}
