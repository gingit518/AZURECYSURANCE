package com.cyberintech.vrisk.server.integration.bigid.client.util;

import com.cyberintech.vrisk.server.integration.bigid.client.common.IBatchResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.PageRequest;

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public final class PageScroller<T> {
	public static final int DEFAULT_PAGE_SIZE = Integer.MAX_VALUE;
	private final int pageSize;
	private final PageableFunction<T> pageableFunction;

	public PageScroller(int pageSize, PageableFunction<T> pageableFunction) {
		this.pageSize = pageSize;
		this.pageableFunction = pageableFunction;
	}

	public PageScroller(PageableFunction<T> pageableFunction) {
		this.pageableFunction = pageableFunction;
		this.pageSize = DEFAULT_PAGE_SIZE;
	}

	public List<T> scroll() {
		int pageNum = 0;
		List<T> accumulator = new LinkedList<>();
		List<T> temp;

		do {
			var pageInfo = PageRequest.of(pageNum++, pageSize);
			temp = ofNullable(pageableFunction.getBatch(pageInfo).getResults()).orElse(emptyList());
			accumulator.addAll(temp);
		}
		while (CollectionUtils.isNotEmpty(temp));

		return accumulator;
	}

	@FunctionalInterface
	public interface PageableFunction<T> {
		IBatchResponse<T> getBatch(PageRequest pageRequest);
	}
}
