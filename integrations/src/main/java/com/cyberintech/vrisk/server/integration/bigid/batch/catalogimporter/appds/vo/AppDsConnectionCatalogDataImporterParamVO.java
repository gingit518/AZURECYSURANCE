package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.vo;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogDataImporterParamVOBase;
import com.cyberintech.vrisk.server.integration.bigid.client.application.vo.ApplicationVO;
import com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo.DatasourceVO;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class AppDsConnectionCatalogDataImporterParamVO extends CatalogDataImporterParamVOBase {

	private final DatasourceVO datasourceVO;
	private final ApplicationVO applicationVO;

	public AppDsConnectionCatalogDataImporterParamVO(Long organizationId, Long userImporterId, DatasourceVO datasourceVO, ApplicationVO applicationVO) {
		super(organizationId, userImporterId);
		this.datasourceVO = datasourceVO;
		this.applicationVO = applicationVO;
	}
}
