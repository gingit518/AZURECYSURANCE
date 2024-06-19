package com.cyberintech.vrisk.server.model.dto.process;

import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Processes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Process Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@Setter
@Getter
@NoArgsConstructor
public class ProcessEditDTO extends ProcessViewDTO {

//	@Schema
//	private Long organizationId;

	@Schema
	private List<BusinessUnitRefDTO> businessUnits;

	@Schema
	private List<DataTypeClassificationRefDTO> dataTypeClassifications;

	@Schema
	private List<DataAssetClassificationRefDTO> dataAssetClassifications;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public ProcessEditDTO(Processes entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Processes entity) {
		super.fromEntity(entity);

		businessUnits = Optional.ofNullable(entity.getBusinessUnitsUsed()).orElse(new HashSet<>()).stream().map(BusinessUnitRefDTO::new).collect(Collectors.toList());
		dataTypeClassifications = Optional.ofNullable(entity.getDataTypeClassifications()).orElse(new HashSet<>()).stream().map(DataTypeClassificationRefDTO::new).collect(Collectors.toList());
		dataAssetClassifications = Optional.ofNullable(entity.getDataAssetClassifications()).orElse(new HashSet<>()).stream().map(DataAssetClassificationRefDTO::new).collect(Collectors.toList());
		systems = Optional.ofNullable(entity.getSystems()).orElse(new HashSet<>()).stream().map(SystemRefDTO::new).collect(Collectors.toList());
	}
}
