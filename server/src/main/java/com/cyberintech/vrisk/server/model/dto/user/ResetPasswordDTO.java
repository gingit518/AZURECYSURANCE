package com.cyberintech.vrisk.server.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Reset password details
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-25
 */
@Setter
@Getter
@ToString(of = {"code"})
@EqualsAndHashCode(of = {"code"})
public class ResetPasswordDTO {

	@Schema
	private String code;

	@Schema
	private String password;

	/**
	 * Default constructor
	 */
	public ResetPasswordDTO() {
	}
}
