package com.cyberintech.vrisk.server.model.dto.agreements;

import com.cyberintech.vrisk.server.model.jpa.entity.OrganizationsAgreements;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Organization Agreement View Entity Definition
 *
 * @author 	 Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since	 2020-01-15
 */
@Setter
@Getter
@NoArgsConstructor
public class OrganizationAgreementEditDTO extends OrganizationAgreementViewDTO {

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public OrganizationAgreementEditDTO(OrganizationsAgreements entity) {
		super(entity);
	}

	@Override
	public void fromEntity(OrganizationsAgreements entity) {
		super.fromEntity(entity);
	}
}
