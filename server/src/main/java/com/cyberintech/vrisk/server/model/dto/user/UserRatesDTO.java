package com.cyberintech.vrisk.server.model.dto.user;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.currency.CurrencyViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.UserRateType;
import com.cyberintech.vrisk.server.model.jpa.entity.UserRates;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;

/**
 * User Rates Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-12-03
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "rateType", "rate"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class UserRatesDTO extends DTOBase<UserRates> {

	@Schema
	private Long id;

	@Schema
	private UserRateType rateType;

	@Schema
	private Double rate;

	@Schema
	private UserRefDTO user;

	@Schema
	private CurrencyViewDTO currency;

	@Schema
	private Date startDate;

	@Schema
	private Date endDate;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public UserRatesDTO(UserRates entity) {
		super(entity);

		if (entity.getCurrency() != null) {
			currency = new CurrencyViewDTO(entity.getCurrency());
		}
	}

}
