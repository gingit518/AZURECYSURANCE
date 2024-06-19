package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.vo;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogDataImporterParamVOBase;
import com.cyberintech.vrisk.server.integration.bigid.client.classifier.vo.FieldClassifierVO;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class FieldClassifierDataImporterParamVO extends CatalogDataImporterParamVOBase {
	private final FieldClassifierVO classifierVO;

	public FieldClassifierDataImporterParamVO(Long organizationId, Long userImporterId, FieldClassifierVO classifierVO) {
		super(organizationId, userImporterId);
		this.classifierVO = classifierVO;
	}
}
