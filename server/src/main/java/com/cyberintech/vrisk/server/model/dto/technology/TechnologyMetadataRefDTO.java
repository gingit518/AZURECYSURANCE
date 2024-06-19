package com.cyberintech.vrisk.server.model.dto.technology;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologiesMetadata;
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
public class TechnologyMetadataRefDTO extends DTOBase<TechnologiesMetadata> {
	@Schema
	private Long id;
	@Schema
	private String key;
	@Schema
	private String value;

	public TechnologyMetadataRefDTO(TechnologiesMetadata technologiesMetadata) {
		super(technologiesMetadata);
	}

	@Override
	public void fromEntity(TechnologiesMetadata technologiesMetadata) {
		this.id = technologiesMetadata.getId();
		this.key = technologiesMetadata.getKey();
		this.value = technologiesMetadata.getValue();
	}
}
