package com.cyberintech.vrisk.server.model.dto.regulations;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.Regulations;
import lombok.*;

/**
 * Regulation Reference Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-07-08
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class RegulationRefDTO extends DTOBase<Regulations> {

	private Long id;

	private String name;

	private String acronym;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public RegulationRefDTO(Regulations entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Regulations entity) {
		id = entity.getId();
		name = entity.getName();
		acronym = entity.getAcronym();
	}
}
