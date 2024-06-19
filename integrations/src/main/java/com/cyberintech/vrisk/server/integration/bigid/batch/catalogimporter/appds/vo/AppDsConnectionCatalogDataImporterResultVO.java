package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.vo;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogDataImporterResultVOBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.ImportAction;
import com.cyberintech.vrisk.server.integration.bigid.client.application.vo.ApplicationVO;
import com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo.DatasourceVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@Setter
public class AppDsConnectionCatalogDataImporterResultVO extends CatalogDataImporterResultVOBase {
	private final DatasourceVO datasourceVO;
	private final ApplicationVO applicationVO;

	private Long systemId;
	private Long technologyId;
	private ImportAction systemAction;

	public AppDsConnectionCatalogDataImporterResultVO(Long organizationId, Long userImporterId, DatasourceVO datasourceVO, ApplicationVO applicationVO) {
		super(organizationId, userImporterId);
		this.datasourceVO = datasourceVO;
		this.applicationVO = applicationVO;
	}
}
