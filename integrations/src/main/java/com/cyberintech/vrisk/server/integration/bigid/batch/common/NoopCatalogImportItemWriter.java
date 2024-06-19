package com.cyberintech.vrisk.server.integration.bigid.batch.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class NoopCatalogImportItemWriter implements ItemWriter<Void> {
	@Override
	public void write(List<? extends Void> list) throws Exception {
		//do nothing
	}
}
