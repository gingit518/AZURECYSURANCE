package com.cyberintech.vrisk.server.model.dto.role;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.Roles;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Role List Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-10-27
 */
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class RoleListDTO extends DTOBase<Roles> {
	private Long id;
	private String name;
	private String description;

	private Boolean isLocked;

	/**
	 * Default constructor
	 */
	public RoleListDTO() {
		super();
	}

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public RoleListDTO(Roles entity) {
		super(entity);
	}

}
