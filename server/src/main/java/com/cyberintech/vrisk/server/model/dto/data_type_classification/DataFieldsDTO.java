package com.cyberintech.vrisk.server.model.dto.data_type_classification;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.DataFields;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Data Fields Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-06-16
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class DataFieldsDTO extends DTOBase<DataFields> {

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
	public DataFieldsDTO(DataFields entity) {
		super(entity);
	}

	@Override
	public void fromEntity(DataFields entity) {
		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
	}
}
