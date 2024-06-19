package com.cyberintech.vrisk.server.model.dto.regulations;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.RegulationAges;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Description
 *
 * @author Oleh Dmytrenko <odmytrenko@dfusiontech.com>
 * @version 0.1.1
 * @since 2022-12-29
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "age"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class RegulationAgeDTO extends DTOBase<RegulationAges> {
	@Schema
	private Long id;

	@Schema
	private Long age;

	@Schema
	private String comments;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public RegulationAgeDTO(RegulationAges entity) {
		id = entity.getId();
		// regulationId = entity.getRegulation().getId();
		age = entity.getAge();
		comments = entity.getComments();
	}

	@Override
	public void fromEntity(RegulationAges entity) {
		id = entity.getId();
		// regulationId = entity.getRegulation().getId();
		age = entity.getAge();
		comments = entity.getComments();
	}
}
