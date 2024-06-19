package com.cyberintech.vrisk.server.model.dto.data_asset_classification;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.jpa.entity.DataAssetClassification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Data Asset Classification View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-26
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class DataAssetClassificationViewDTO extends DTOWithMetaData<DataAssetClassification> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private Double threshold;

	@Schema
	private boolean readOnly;

	@Schema
	private Set<DataAssetClassificationMetadataRefDTO> entityMetadata;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public DataAssetClassificationViewDTO(DataAssetClassification entity) {
		super(entity);
	}

	@Override
	public void fromEntity(DataAssetClassification entity) {
//		super.fromEntity(entity);

		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
		threshold = entity.getThreshold();

		readOnly = entity.getOrganizationId() == null;

		entityMetadata = Optional.ofNullable(entity.getMetadata())
			.stream()
			.flatMap(Set::stream).map(DataAssetClassificationMetadataRefDTO::new).collect(Collectors.toSet());
	}
}
