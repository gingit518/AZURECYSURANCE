package com.cyberintech.vrisk.server.model.dto.user;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.domains.IdpType;
import com.cyberintech.vrisk.server.model.jpa.entity.IdpUsers;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Idp User Entity Definition
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-05-14
 */
@Setter
@Getter
@ToString(of = {"id", "email"})
@EqualsAndHashCode(of = {"id", "email"}, callSuper = false)
@NoArgsConstructor
public class IdpUserDTO extends DTOBase<IdpUsers> {

	@Schema
	private Long id;

	@Schema
	private IdpType idpId;

	@Schema
	private String userIdentity;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public IdpUserDTO(IdpUsers entity) {
		super(entity);
	}

	@Override
	public void fromEntity(IdpUsers entity) {
		id = entity.getId();
		idpId = entity.getIdpId();
		userIdentity = entity.getUserIdentity();
	}

}
