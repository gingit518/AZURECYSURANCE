package com.cyberintech.vrisk.server.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Forget password details
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-25
 */
@Setter
@Getter
@ToString(of = {"email"})
@EqualsAndHashCode(of = {"email"}, callSuper = false)
public class ForgetPasswordDTO {

	@Schema
	private String email;

	/**
	 * Default constructor
	 */
	public ForgetPasswordDTO() {
	}
}
