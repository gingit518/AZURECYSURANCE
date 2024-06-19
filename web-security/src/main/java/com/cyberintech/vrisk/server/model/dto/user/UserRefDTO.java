package com.cyberintech.vrisk.server.model.dto.user;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * User Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "email"})
@EqualsAndHashCode(of = {"id", "email"}, callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRefDTO extends DTOBase<Users> {

	private Long id;

	private String fullName;

	private String firstName;

	private String lastName;

	private String email;

	/**
	 * Entity based constructor
	 *
	 * @param users
	 */
	public UserRefDTO(Users users) {
		super(users);
	}

	@Override
	public void fromEntity(Users entity) {
		id = entity.getId();
		fullName = entity.getFullName();
		firstName = entity.getFirstName();
		lastName = entity.getLastName();
		email = entity.getEmail();
	}
}
