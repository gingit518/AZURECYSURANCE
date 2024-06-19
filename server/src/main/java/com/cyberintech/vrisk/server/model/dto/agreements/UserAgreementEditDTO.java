package com.cyberintech.vrisk.server.model.dto.agreements;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.user.UserDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.OrganizationsAgreements;
import com.cyberintech.vrisk.server.model.jpa.entity.UsersAgreements;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;


/**
 * User Agreement View Entity Definition
 *
 * @author 	 Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since	 2020-01-17
 */
@Setter
@Getter
@NoArgsConstructor
public class UserAgreementEditDTO extends UserAgreementViewDTO {

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public UserAgreementEditDTO(UsersAgreements entity) {
		super(entity);
	}

	/**
	 * Organization Agreement based constructor
	 * @param entity
	 */
	public UserAgreementEditDTO(OrganizationsAgreements entity) {
		super(entity);
	}

	@Override
	public void fromEntity(UsersAgreements entity) {
		super.fromEntity(entity);
	}
}
