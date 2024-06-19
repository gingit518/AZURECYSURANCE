package com.cyberintech.vrisk.server.integration.bigid.batch.management.vo;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogImportJobParamsVO;

public class CatalogImportLaunchJobResponseVO extends LaunchJobResponseVOBase<CatalogImportJobParamsVO, CatalogImportJobVO> {
	public CatalogImportLaunchJobResponseVO(CatalogImportJobParamsVO request, CatalogImportJobVO job) {
		super(request, job);
	}
}
