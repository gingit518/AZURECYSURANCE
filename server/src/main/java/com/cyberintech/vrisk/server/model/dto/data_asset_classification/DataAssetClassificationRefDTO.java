package com.cyberintech.vrisk.server.model.dto.data_asset_classification;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.DataAssetClassification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

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
public class DataAssetClassificationRefDTO extends DTOBase<DataAssetClassification> {

	@Schema
	private Long id;

	@Schema
	private String name;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public DataAssetClassificationRefDTO(DataAssetClassification entity) {
		super(entity);
	}

	@Override
	public void fromEntity(DataAssetClassification entity) {
//		super.fromEntity(entity);
		this.id = entity.getId();
		this.name = entity.getName();
	}
}
