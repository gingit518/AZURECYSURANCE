package com.cyberintech.vrisk.server.model.dto.organization;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.domains.TwoFactorType;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Organization Reference Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-10
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationRefDTO extends DTOBase<Organizations> {

	private Long id;
	private String name;
	private String description;
	private OrganizationType organizationType;

	private Boolean useMultiFactorAuth;
	private TwoFactorType twoFactorType;

	@Schema
	private DocumentDTO logoDocument;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public OrganizationRefDTO(Organizations entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Organizations entity) {
		// super.fromEntity(entity);
		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
		organizationType = entity.getOrganizationType();

		useMultiFactorAuth = entity.getUseMultiFactorAuth();
		twoFactorType = entity.getTwoFactorType();
	}
}
