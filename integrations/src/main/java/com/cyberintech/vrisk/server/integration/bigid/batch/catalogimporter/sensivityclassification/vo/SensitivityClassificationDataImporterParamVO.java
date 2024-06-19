package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.vo;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogDataImporterParamVOBase;
import com.cyberintech.vrisk.server.integration.bigid.client.sensitivityclassification.vo.SCConfigVO;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class SensitivityClassificationDataImporterParamVO extends CatalogDataImporterParamVOBase {
	private final SCConfigVO scConfigVO;

	public SensitivityClassificationDataImporterParamVO(Long organizationId, Long userImporterId,
														SCConfigVO scConfigVO) {
		super(organizationId, userImporterId);
		this.scConfigVO = scConfigVO;
	}
}
