package com.cyberintech.vrisk.server.model.dto.city;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.City;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * City View Entity Definition
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
public class CityViewDTO extends DTOBase<City> {

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
	public CityViewDTO(City entity) {
		super(entity);
	}

}
