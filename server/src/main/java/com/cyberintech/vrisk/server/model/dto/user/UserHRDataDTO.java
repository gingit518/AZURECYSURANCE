package com.cyberintech.vrisk.server.model.dto.user;

import com.cyberintech.vrisk.server.model.dto.currency.CurrencyViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.UserEmploymentType;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * User Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-12-03
 */
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"}, callSuper = true)
public class UserHRDataDTO extends UserUpdateDTO {

	private UserEmploymentType employmentType;

	private List<UserRatesDTO> userRates = new ArrayList<>();

	public UserHRDataDTO() {
		super();
	}

	public UserHRDataDTO(Users entity) {
		super(entity);

		userRates = Optional.ofNullable(entity.getUserRates()).orElse(new HashSet<>()).stream().map(userRate -> new UserRatesDTO(userRate)).collect(Collectors.toList());
	}

}
