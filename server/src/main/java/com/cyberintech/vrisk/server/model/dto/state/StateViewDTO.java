package com.cyberintech.vrisk.server.model.dto.state;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.State;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * State View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class StateViewDTO extends DTOBase<State> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String code;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public StateViewDTO(State entity) {
		super(entity);
	}

}
