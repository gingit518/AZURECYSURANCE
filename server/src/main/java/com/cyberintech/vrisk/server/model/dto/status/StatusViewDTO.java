package com.cyberintech.vrisk.server.model.dto.status;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.Status;
import com.cyberintech.vrisk.server.model.jpa.entity.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Status View Entity Definition
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
public class StatusViewDTO extends DTOBase<Status> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public StatusViewDTO(Status entity) {
		super(entity);
	}

}
