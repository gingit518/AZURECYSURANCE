package com.cyberintech.vrisk.server.model.dto.city;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.City;
import com.cyberintech.vrisk.server.model.jpa.entity.Country;
import com.cyberintech.vrisk.server.model.jpa.entity.State;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * City Edit Entity Definition
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
public class CityEditDTO extends DTOBase<City> {

	@Schema
	private Long id;

	@Schema
	private ItemViewDTO<Country> country;

	@Schema
	private ItemViewDTO<State> state;

	@Schema
	private String name;

	@Schema
	private String code;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public CityEditDTO(City entity) {
		super(entity);
	}

	@Override
	public void fromEntity(City city) {
		super.fromEntity(city);

		if (city.getCountry() != null) {
			this.country = new ItemViewDTO<>(city.getCountry());
		}

		if (city.getState() != null) {
			this.state = new ItemViewDTO<>(city.getState());
		}
	}
}
