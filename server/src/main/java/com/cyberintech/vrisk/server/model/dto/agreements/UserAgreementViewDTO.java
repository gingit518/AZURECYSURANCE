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
@ToString(of = {"id", "answer"})
@EqualsAndHashCode(of = {"id", "answer"}, callSuper = false)
public class UserAgreementViewDTO extends DTOBase<UsersAgreements> {

	@Schema
	private Long id;

	@Schema
	private Boolean answer;

	@Schema
	private Boolean isAnswered;

	@Schema
	private Date updatedAt;

	@Schema
	private UserDTO user;

	@Schema
	private OrganizationAgreementViewDTO organizationAgreement;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public UserAgreementViewDTO(UsersAgreements entity) {
		super(entity);
	}

	/**
	 * Organization Agreement based constructor
	 *
	 * @param entity
	 */
	public UserAgreementViewDTO(OrganizationsAgreements entity) {

		this.id = null;
		this.answer = null;
		this.isAnswered = false;
		this.updatedAt = null;
		this.user = null;
		this.organizationAgreement = new OrganizationAgreementViewDTO(entity);
	}

	@Override
	public void fromEntity(UsersAgreements entity) {

		this.id = entity.getId();
		this.answer = entity.getAnswer();
		this.isAnswered = entity.getIsAnswered();
		this.updatedAt = entity.getUpdatedAt();

		if (entity.getUser() != null) {
			this.user = new UserDTO(entity.getUser());
		}

		if (entity.getOrganizationAgreement() != null) {
			this.organizationAgreement = new OrganizationAgreementViewDTO(entity.getOrganizationAgreement());
		}
	}
}
