package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.vo;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogDataImporterResultVOBase;
import com.cyberintech.vrisk.server.integration.bigid.client.sensitivityclassification.vo.SCConfigVO;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationRefDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class SensitivityClassificationDataImporterResultVO extends CatalogDataImporterResultVOBase {
	private final SCConfigVO scConfigVO;

	private final List<DataAssetClassificationRefDTO> createdDataAssetClassifiers;
	private final List<DataAssetClassificationRefDTO> syncedDataAssetClassifiers;

	public SensitivityClassificationDataImporterResultVO(Long organizationId, Long userImporterId, SCConfigVO scConfigVO,
														 List<DataAssetClassificationRefDTO> createdDataAssetClassifiers, List<DataAssetClassificationRefDTO> syncedDataAssetClassifiers) {
		super(organizationId, userImporterId);
		this.scConfigVO = scConfigVO;
		this.createdDataAssetClassifiers = createdDataAssetClassifiers;
		this.syncedDataAssetClassifiers = syncedDataAssetClassifiers;
	}

}
