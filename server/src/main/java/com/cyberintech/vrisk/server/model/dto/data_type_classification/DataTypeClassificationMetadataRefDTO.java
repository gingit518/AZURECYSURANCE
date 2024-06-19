package com.cyberintech.vrisk.server.model.dto.data_type_classification;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.DataTypeClassificationMetadata;
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
public class DataTypeClassificationMetadataRefDTO extends DTOBase<DataTypeClassificationMetadata> {
	@Schema
	private Long id;
	@Schema
	private String key;
	@Schema
	private String value;

	public DataTypeClassificationMetadataRefDTO(DataTypeClassificationMetadata metadata) {
		super(metadata);
	}

	@Override
	public void fromEntity(DataTypeClassificationMetadata metadata) {
		this.id = metadata.getId();
		this.key = metadata.getKey();
		this.value = metadata.getValue();
	}
}
