package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.vo;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogDataImporterParamVOBase;
import com.cyberintech.vrisk.server.integration.bigid.client.application.vo.ApplicationVO;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ApplicationCatalogDataImporterParamVO extends CatalogDataImporterParamVOBase {
	private final ApplicationVO applicationVO;

	public ApplicationCatalogDataImporterParamVO(Long organizationId, Long userImporterId,
												 ApplicationVO applicationVO) {
		super(organizationId, userImporterId);
		this.applicationVO = applicationVO;
	}

}
