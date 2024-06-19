package com.cyberintech.vrisk.server.model.dto.currency;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Currency View Entity Definition
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
public class CurrencyViewDTO extends DTOBase<Currency> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String code;

	@Schema
	private String symbol;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public CurrencyViewDTO(Currency entity) {
		super(entity);
	}

}
