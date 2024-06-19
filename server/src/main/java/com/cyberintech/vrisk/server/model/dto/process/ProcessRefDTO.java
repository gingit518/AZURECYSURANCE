package com.cyberintech.vrisk.server.model.dto.process;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.Processes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Process Reference Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-10
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class ProcessRefDTO extends DTOBase<Processes> {

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
	public ProcessRefDTO(Processes entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Processes entity) {
		// super.fromEntity(entity);
		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
	}
}
