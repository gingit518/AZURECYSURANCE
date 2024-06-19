package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.vo;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogDataImporterParamVOBase;
import com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo.DatasourceVO;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class DatasourceCatalogDataImporterParamVO extends CatalogDataImporterParamVOBase {
	private final DatasourceVO datasourceVO;

	public DatasourceCatalogDataImporterParamVO(Long organizationId, Long userImporterId,
												DatasourceVO datasourceVO) {
		super(organizationId, userImporterId);
		this.datasourceVO = datasourceVO;
	}
}
