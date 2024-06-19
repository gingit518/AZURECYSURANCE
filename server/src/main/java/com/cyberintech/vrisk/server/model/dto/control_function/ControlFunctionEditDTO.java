package com.cyberintech.vrisk.server.model.dto.control_function;

import com.cyberintech.vrisk.server.model.jpa.entity.ControlFunctions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Control Function Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-29
 */
@Setter
@Getter
@NoArgsConstructor
public class ControlFunctionEditDTO extends ControlFunctionViewDTO {

//	@Schema
//	private Long organizationId;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public ControlFunctionEditDTO(ControlFunctions entity) {
		super(entity);
	}

	@Override
	public void fromEntity(ControlFunctions entity) {
		super.fromEntity(entity);
	}
}
