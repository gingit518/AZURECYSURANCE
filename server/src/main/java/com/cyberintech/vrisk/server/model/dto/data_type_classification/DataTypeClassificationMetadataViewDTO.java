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
public class DataTypeClassificationMetadataViewDTO extends DTOBase<DataTypeClassificationMetadata> {
	@Schema
	private Long id;
	@Schema
	private DataTypeClassificationRefDTO dataTypeClassification;
	@Schema
	private String key;
	@Schema
	private String value;

	public DataTypeClassificationMetadataViewDTO(DataTypeClassificationMetadata metadata) {
		super(metadata);
	}

	@Override
	public void fromEntity(DataTypeClassificationMetadata metadata) {
		this.id = metadata.getId();
		this.dataTypeClassification = new DataTypeClassificationRefDTO(metadata.getDataTypeClassification());
		this.key = metadata.getKey();
		this.value = metadata.getValue();
	}
}
