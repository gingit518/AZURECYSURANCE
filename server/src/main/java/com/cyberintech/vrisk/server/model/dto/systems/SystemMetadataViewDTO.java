package com.cyberintech.vrisk.server.model.dto.systems;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.SystemsMetadata;
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
public class SystemMetadataViewDTO extends DTOBase<SystemsMetadata> {
	@Schema
	private Long id;
	@Schema
	private SystemRefDTO system;
	@Schema
	private String key;
	@Schema
	private String value;

	public SystemMetadataViewDTO(SystemsMetadata systemsMetadata) {
		super(systemsMetadata);
	}

	@Override
	public void fromEntity(SystemsMetadata systemsMetadata) {
		this.id = systemsMetadata.getId();
		this.system = new SystemRefDTO(systemsMetadata.getSystem());
		this.key = systemsMetadata.getKey();
		this.value = systemsMetadata.getValue();
	}
}
