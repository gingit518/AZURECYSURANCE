package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch;

import com.cyberintech.vrisk.server.service.storage.StorageDocumentsService;
import com.cyberintech.vrisk.server.service.storage.vo.UploadRequestVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogDataImporterResultVOBase;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.springframework.batch.core.annotation.BeforeChunk;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants.CHUNK_COUNTER_PARAM;
import static com.cyberintech.vrisk.server.integration.bigid.batch.util.DateTimeFormatterUtil.UTC_TZ;
import static com.cyberintech.vrisk.server.integration.bigid.batch.util.DateTimeFormatterUtil.YYYY_MM_DD;
import static com.cyberintech.vrisk.server.integration.bigid.batch.util.DateTimeFormatterUtil.YYYY_MM_DD_HH_MM_SS_FILE_NAME;

public abstract class CsvImportItemWriterBase<T extends CatalogDataImporterResultVOBase> implements ItemWriter<T> {

	private final StorageDocumentsService s3UploadHelper;
	private ChunkContext chunkContext;

	protected CsvImportItemWriterBase(StorageDocumentsService s3UploadHelper) {
		this.s3UploadHelper = s3UploadHelper;
	}


	@Override
	public void write(List<? extends T> items) throws Exception {
		getLogger().info("Chunk Import result: {}", items);

		if (items == null || items.isEmpty()) {
			return;
		}

		long chunkCounter = getAndIncrementChunkCounter();
		Long organizationId = items.get(0).getOrganizationId();

		LocalDateTime jobStartTime = getJobStartTime();
		String destinationPath = getCsvDestinationPath(getJobId(), organizationId, getAssetName(), jobStartTime);
		String fileName = String.format("%s-chunk-%s.csv", YYYY_MM_DD_HH_MM_SS_FILE_NAME.format(jobStartTime),
			chunkCounter);

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(outputStream),
				CSVFormat.Builder.create().setHeader(getCsvHeaders()).build())) {
				for (T item : items) {
					writeAsCsvRow(printer, item);
				}
			}

			UploadRequestVO s3UploadRequest = new UploadRequestVO(
				destinationPath, fileName, outputStream.size(), new ByteArrayInputStream(outputStream.toByteArray())
			);
			s3UploadHelper.uploadSilently(s3UploadRequest);
		}
	}

	protected abstract void writeAsCsvRow(CSVPrinter csvPrinter, T item);

	protected abstract String[] getCsvHeaders();

	protected abstract Logger getLogger();

	protected String getCsvDestinationPath(Long jobId,
										   Long organizationId, String assetName,
										   LocalDateTime utcTime) {
		return String.format("%s/%s/%s/job-%s/%s",
			BatchConstants.CATALOG_IMPORT_JOB_NAME,
			organizationId,
			YYYY_MM_DD.format(utcTime),
			jobId,
			assetName);
	}

	protected abstract String getAssetName();

	@BeforeChunk
	public void beforeChunk(ChunkContext chunkContext) {
		this.chunkContext = chunkContext;
	}

	private long getAndIncrementChunkCounter() {
		ExecutionContext executionContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();
		long chunkCounter = executionContext.getLong(CHUNK_COUNTER_PARAM, 1);
		executionContext.putLong(CHUNK_COUNTER_PARAM, chunkCounter + 1);
		return chunkCounter;
	}

	private LocalDateTime getJobStartTime() {
		return LocalDateTime.ofInstant(chunkContext.getStepContext().getStepExecution().getStartTime().toInstant(),
			ZoneId.of(UTC_TZ));
	}

	private long getJobId() {
		return chunkContext.getStepContext().getStepExecution().getJobExecutionId();
	}

}
