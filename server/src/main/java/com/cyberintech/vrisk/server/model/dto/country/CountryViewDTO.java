package com.cyberintech.vrisk.server.model.dto.country;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.Country;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Country View Entity Definition
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
public class CountryViewDTO extends DTOBase<Country> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String code;

	@Schema
	private String phoneCode;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public CountryViewDTO(Country entity) {
		super(entity);
	}

}
