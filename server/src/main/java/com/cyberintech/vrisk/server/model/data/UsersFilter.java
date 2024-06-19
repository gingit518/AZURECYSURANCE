package com.cyberintech.vrisk.server.model.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Implementation of User Filtering Logic
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@NoArgsConstructor
@Setter
@Getter
public class UsersFilter extends NameFilter {

	@Schema(name = "List of User role codes",
		allowableValues = "USER, ADMIN, CEO, CRO, CISO, CLM, DPO, REM, SYSOWN, INFOSEC, TSM, PM, BUO, AUD, RISKMAN, PRCOWN, ORGADMIN"
		)
	private List<String> roles;

	@Schema
	private Long businessUnitId;

	@Schema
	private String firstName;

	@Schema
	private String lastName;

	@Schema
	private String email;

	@Schema
	private Long roleId;

	@Schema
	private Long organizationId;

	@Schema
	private Boolean isDeleted;
}
