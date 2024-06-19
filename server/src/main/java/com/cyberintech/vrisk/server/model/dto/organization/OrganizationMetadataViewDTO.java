package com.cyberintech.vrisk.server.model.dto.organization;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.OrganizationsMetadata;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = {"key"}, callSuper = false)
public class OrganizationMetadataViewDTO extends DTOBase<OrganizationsMetadata> {
	@Schema
	private Long id;
	@Schema
	private OrganizationRefDTO organization;
	@Schema
	private String key;
	@Schema
	private String value;

	public OrganizationMetadataViewDTO(OrganizationsMetadata metadata) {
		super(metadata);
	}

	@Override
	public void fromEntity(OrganizationsMetadata metadata) {
		this.id = metadata.getId();
		this.organization = new OrganizationRefDTO(metadata.getOrganization());
		this.key = metadata.getKey();
		this.value = metadata.getValue();
	}
}
