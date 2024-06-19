package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.CatalogDataImporterBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.CatalogDataImporterFactoryBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogDataImporterParamVOBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogDataImporterResultVOBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogDataImporterStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.batch.item.ItemProcessor;

@RequiredArgsConstructor
public abstract class DataImportItemProcessorBase<P extends CatalogDataImporterParamVOBase,
	R extends CatalogDataImporterResultVOBase,
	I extends CatalogDataImporterBase<P, R>,
	F extends CatalogDataImporterFactoryBase<I, P>> implements ItemProcessor<P, R> {

	private final F factory;

	@Override
	public R process(P item) throws Exception {
		getLogger().info("Processing item. Id = {}, data = {}.", item.getId(), item);
		validateOrFail(item);
		I importer = factory.create(item);
		try {
			importer.process();
			getLogger().info("Item was processed. Id = {}", item.getId());
		} catch (Exception ex) {
			getLogger().warn("Can not process item: Id = {}, data = {}. Exception was thrown.", item.getId(), item, ex);
			importer.getResult().setErrorMessage(ex.getMessage());
			importer.getResult().setStatus(CatalogDataImporterStatus.FAILED);
		}
		return importer.getResult();
	}

	protected abstract void validateOrFail(P item);

	protected abstract Logger getLogger();
}
