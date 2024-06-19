package com.cyberintech.vrisk.server.service.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobStorageException;
import com.cyberintech.vrisk.server.model.jpa.domains.DocumentType;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import com.cyberintech.vrisk.server.service.storage.vo.UploadRequestVO;
import com.cyberintech.vrisk.server.service.storage.vo.UploadResponseVO;
import com.cyberintech.vrisk.server.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@Profile("azure")
public class StorageDocumentsServiceAzure implements StorageDocumentsService {

	@Value("${cloud.azure.storage.containerUrl:}")
	private String azureContainerUrl;

	@Value("${cloud.azure.storage.blobContainer:}")
	private String blobContainer;

	public StorageDocumentsServiceAzure() {
	}

	protected BlobContainerClient blobContainerClient(DocumentType documentType) {
		BlobContainerClient containerClient;

		if (DocumentType.IMAGE.equals(documentType) || DocumentType.PUBLIC.equals(documentType)) {
			containerClient = BeanUtil.getBean("blobContainerClientPublicRead", BlobContainerClient.class);
		} else {
			containerClient = BeanUtil.getBean("blobContainerClient", BlobContainerClient.class);
		}

		return containerClient;
	}

	/**
     * Returns Storage URL for the System
	 *
	 * @return
	 */
	@Override
	public String getStorageURL() {
		String bucketUrl = blobContainerClient(DocumentType.PUBLIC).getBlobContainerUrl();

		if (!bucketUrl.endsWith("/")) {
			bucketUrl += "/";
		}

		return bucketUrl;
	}

	@Override
	public UploadResponseVO upload(UploadRequestVO uploadRequest) {
		UploadResponseVO result = new UploadResponseVO();

		String fileKey = (StringUtils.isNotEmpty(uploadRequest.getDestinationPath())) ? uploadRequest.getDestinationPath() + "/" + uploadRequest.getFileName() : uploadRequest.getFileName();

		BlobContainerClient blobContainerClient = blobContainerClient(uploadRequest.getDocumentType());
		BlobClient blobClient = blobContainerClient.getBlobClient(fileKey);
		blobClient.upload(uploadRequest.getInputStream());

		// Set Content-Type with Http Headers
		if (uploadRequest.getContentType() != null) {
			BlobHttpHeaders blobHttpHeaders = new BlobHttpHeaders();
			blobHttpHeaders.setContentType(uploadRequest.getContentType());
			blobClient.setHttpHeaders(blobHttpHeaders);
		}

		return result;
	}

	@Override
	public Optional<UploadResponseVO> uploadSilently(UploadRequestVO uploadRequest) {
		try {
			return Optional.of(upload(uploadRequest));
		} catch (Exception ex) {
			log.warn("Can not upload {} to Azure Storage Container {}.", uploadRequest, blobContainer, ex);
			return Optional.empty();
		}
	}

	@Override
	@NotNull
	public ByteArrayOutputStream getRemoteFileContentOutputStream(String fileName, DocumentType documentType) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		BlobContainerClient blobContainerClient = blobContainerClient(documentType != null ? documentType : DocumentType.PRIVATE);
		BlobClient blobClient = blobContainerClient.getBlobClient(fileName);

		try {
			blobClient.downloadStream(byteArrayOutputStream);
		} catch (BlobStorageException exception) {
			if (exception.getErrorCode().equals(BlobErrorCode.BLOB_NOT_FOUND)) {
				throw new ItemNotFoundException("File not found: " + fileName);
			} else {
				throw exception;
			}
		}

		return byteArrayOutputStream;
	}

}
