package com.cyberintech.vrisk.server.model.dto.agreements;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.OrganizationsAgreements;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class OrganizationAgreementViewDTO extends DTOWithMetaData<OrganizationsAgreements> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String content;

	@Schema
	private List<OrganizationRefDTO> organizations;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public OrganizationAgreementViewDTO(OrganizationsAgreements entity) {
		super(entity);
	}

	@Override
	public void fromEntity(OrganizationsAgreements entity) {
		this.id = entity.getId();
		this.name = entity.getName();
		this.content = entity.getContent();

		organizations = Optional.ofNullable(entity.getOrganizations()).orElse(new HashSet<>()).stream()
			.map(OrganizationRefDTO::new).collect(Collectors.toList());
	}

}
