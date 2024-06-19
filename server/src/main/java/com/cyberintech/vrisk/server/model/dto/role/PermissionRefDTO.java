package com.cyberintech.vrisk.server.model.dto.role;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.Permissions;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Permission Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-19
 */
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionRefDTO extends DTOBase<Permissions> {

	private Long id;
	private String name;

	/**
	 * Default constructor
	 */
	public PermissionRefDTO() {
		super();
	}

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public PermissionRefDTO(Permissions entity) {
		super(entity);
	}

}
