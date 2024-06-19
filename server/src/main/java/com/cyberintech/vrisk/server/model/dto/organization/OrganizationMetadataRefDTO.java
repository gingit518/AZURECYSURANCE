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
public class OrganizationMetadataRefDTO extends DTOBase<OrganizationsMetadata> {
	@Schema
	private Long id;
	@Schema
	private String key;
	@Schema
	private String value;

	public OrganizationMetadataRefDTO(OrganizationsMetadata organizationsMetadata) {
		super(organizationsMetadata);
	}

	@Override
	public void fromEntity(OrganizationsMetadata organizationsMetadata) {
		this.id = organizationsMetadata.getId();
		this.key = organizationsMetadata.getKey();
		this.value = organizationsMetadata.getValue();
	}
}
