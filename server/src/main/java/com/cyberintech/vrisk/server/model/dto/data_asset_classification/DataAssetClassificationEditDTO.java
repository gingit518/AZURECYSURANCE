package com.cyberintech.vrisk.server.model.dto.data_asset_classification;

import com.cyberintech.vrisk.server.model.jpa.entity.DataAssetClassification;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Asset Classification Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-26
 */
@Setter
@Getter
@NoArgsConstructor
public class DataAssetClassificationEditDTO extends DataAssetClassificationViewDTO {

//	@Schema
//	private Long organizationId;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public DataAssetClassificationEditDTO(DataAssetClassification entity) {
		super(entity);
	}

	@Override
	public void fromEntity(DataAssetClassification entity) {
		super.fromEntity(entity);
	}
}
