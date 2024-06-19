package com.cyberintech.vrisk.server.model.dto.field_classifier;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.FieldClassifiersMetadata;
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
public class FieldClassifierMetadataRefDTO extends DTOBase<FieldClassifiersMetadata> {
	@Schema
	private Long id;
	@Schema
	private String key;
	@Schema
	private String value;

	public FieldClassifierMetadataRefDTO(FieldClassifiersMetadata fieldClassifiersMetadata) {
		super(fieldClassifiersMetadata);
	}

	@Override
	public void fromEntity(FieldClassifiersMetadata fieldClassifiersMetadata) {
		this.id = fieldClassifiersMetadata.getId();
		this.key = fieldClassifiersMetadata.getKey();
		this.value = fieldClassifiersMetadata.getValue();
	}
}
