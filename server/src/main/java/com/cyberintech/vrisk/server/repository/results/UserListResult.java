package com.cyberintech.vrisk.server.repository.results;

import com.cyberintech.vrisk.server.model.jpa.entity.BusinessUnits;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Users List Result Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-13
 */
@Getter
@Setter
@NoArgsConstructor
@Data
public class UserListResult {

	private Users user;

	private BusinessUnits businessUnit;

	public UserListResult(Users user, BusinessUnits businessUnit) {
		this.user = user;
		this.businessUnit = businessUnit;
	}
}
