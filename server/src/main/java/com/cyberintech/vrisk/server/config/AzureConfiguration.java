package com.cyberintech.vrisk.server.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.*;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.time.OffsetDateTime;
import java.util.Collections;

@Slf4j
@Configuration
@Profile("azure")
public class AzureConfiguration {

	@Value("${cloud.azure.storage.accountEndpoint:}")
	private String storageAccountEndpoint;

	@Value("${cloud.azure.storage.sasToken:}")
	private String sasToken;

	@Value("${cloud.azure.storage.connectionString:}")
	private String connectionString;

	@Value("${cloud.azure.storage.blobContainer:}")
	private String blobContainer;

	@Value("${cloud.azure.storage.blobContainerRead:}")
	private String blobContainerRead;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Getter
	private Boolean isPublicAccessPermitted = true;

	@Bean
	public BlobServiceClient blobServiceClient() {
		/* Create a new BlobServiceClient with a SAS Token */
		BlobServiceClientBuilder clientBuilder = new BlobServiceClientBuilder().endpoint(storageAccountEndpoint);
		if (StringUtils.isNotEmpty(connectionString)) {
			clientBuilder.connectionString(connectionString);
		} else {
			clientBuilder.sasToken(sasToken);
		}
		BlobServiceClient blobServiceClient = clientBuilder.buildClient();

		return blobServiceClient;
	}

	@Bean(name = "blobContainerClient")
	public BlobContainerClient blobContainerClient() {
		BlobContainerClient containerClient = null;

		BlobServiceClient blobServiceClient = blobServiceClient();

		/* Create a new container client */
		try {
			containerClient = blobServiceClient.createBlobContainerIfNotExists(blobContainer);
		} catch (BlobStorageException exception) {
			// The container may already exist, so don't throw an error
			if (!exception.getErrorCode().equals(BlobErrorCode.CONTAINER_ALREADY_EXISTS)) {
				// throw exception;
				log.error("Failed to CREATE BLOB container for Azure: " + blobContainer);
			}
		}

		try {
			if (containerClient == null) containerClient = blobServiceClient.getBlobContainerClient(blobContainer);
		} catch (BlobStorageException exception) {
			// The container may already exist, so don't throw an error
			// throw exception;
			log.error("Failed to GET BLOB container for Azure: " + blobContainer);
		}

		return containerClient;
	}

	@Bean(name = "blobContainerClientPublicRead")
	public BlobContainerClient blobContainerClientPublicRead() {
		BlobContainerClient containerClient = null;

		BlobServiceClient blobServiceClient = blobServiceClient();

		/*
		BlobSignedIdentifier identifier = new BlobSignedIdentifier()
			.setId("AccessRead")
			.setAccessPolicy(new BlobAccessPolicy()
				.setStartsOn(OffsetDateTime.now())
				.setExpiresOn(OffsetDateTime.now().plusYears(5))
				.setPermissions("r"));
		 */

		/* Create a new container client */
		try {
			containerClient = blobServiceClient.createBlobContainerIfNotExists(blobContainerRead);
			// containerClient.setAccessPolicy(PublicAccessType.BLOB, Collections.singletonList(identifier));
			isPublicAccessPermitted = false;
			applicationProperties.setPublicAccessPermitted(false);
		} catch (BlobStorageException exception) {
			// The container may already exist, so don't throw an error
			if (exception.getErrorCode().equals(BlobErrorCode.fromString("PublicAccessNotPermitted"))) {
				isPublicAccessPermitted = false;
				applicationProperties.setPublicAccessPermitted(false);
			} else if (!exception.getErrorCode().equals(BlobErrorCode.CONTAINER_ALREADY_EXISTS)) {
				log.error("Failed to CREATE BLOB container for Azure: " + blobContainerRead);
				// throw exception;
			}
		}

		try {
			if (containerClient == null) containerClient = blobServiceClient.getBlobContainerClient(blobContainerRead);
			log.info(String.format("Set Access Policy to 'Public read access for blobs only' for %s.", blobContainerRead));
		} catch (UnsupportedOperationException exception) {
			log.warn(String.format("Set Access Policy failed because: %s", exception.getMessage()));
			throw exception;
		} catch (BlobStorageException exception) {
			// The container may already exist, so don't throw an error
			log.error("Failed to GET BLOB container for Azure: " + blobContainerRead);
			// throw exception;
		}

		return containerClient;
	}

}
