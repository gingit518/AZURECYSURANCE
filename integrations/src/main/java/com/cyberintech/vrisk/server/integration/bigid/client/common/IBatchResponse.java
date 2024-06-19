package com.cyberintech.vrisk.server.integration.bigid.client.common;

import java.util.List;

public interface IBatchResponse<T> {
	List<T> getResults();

	long getTotalRowsCounter();
}
