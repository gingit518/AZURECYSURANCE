package com.cyberintech.vrisk.server.integration.bigid.client.datacatalog.vo;

import com.cyberintech.vrisk.server.integration.bigid.client.common.IBatchResponse;
import lombok.Data;

import java.util.List;

@Data
public class DataCatalogBatchResponseVO implements IBatchResponse<DataCatalogVO> {
	private List<DataCatalogVO> results;
	private long totalRowsCounter;
}
