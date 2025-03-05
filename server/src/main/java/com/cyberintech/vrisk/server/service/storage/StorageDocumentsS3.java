package com.cyberintech.vrisk.server.service.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.cyberintech.vrisk.server.model.jpa.domains.DocumentType;
import com.cyberintech.vrisk.server.service.storage.vo.UploadRequestVO;
import com.cyberintech.vrisk.server.service.storage.vo.UploadResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@Profile("!azure")
public class StorageDocumentsS3 implements StorageDocumentsService {

	@Autowired
	@Lazy
	private AmazonS3 s3Client;

	@Value("${cloud.aws.s3.bucket:}")
	private String s3Bucket;

	public StorageDocumentsS3() {
	}

	/**
     * Returns Storage URL for the System
	 *
	 * @return
	 */
	@Override
	public String getStorageURL() {
		String bucketUrl = s3Client.getUrl(s3Bucket, "").toString();

		if (!bucketUrl.endsWith("/")) {
			bucketUrl += "/";
		}

		return bucketUrl;
	}

	@Override
	public UploadResponseVO upload(UploadRequestVO uploadRequest) {
		UploadResponseVO result = new UploadResponseVO();

		String fileKey = (StringUtils.isNotEmpty(uploadRequest.getDestinationPath())) ? uploadRequest.getDestinationPath() + "/" + uploadRequest.getFileName() : uploadRequest.getFileName();

		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentLength(uploadRequest.getSize());
		PutObjectRequest putObjectRequest = new PutObjectRequest(s3Bucket, fileKey, uploadRequest.getInputStream(), objectMetadata);
		if (DocumentType.IMAGE.equals(uploadRequest.getDocumentType()) || DocumentType.PUBLIC.equals(uploadRequest.getDocumentType())) {
			result.setIsPublic(true);
			putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
		} else {
			putObjectRequest.withCannedAcl(CannedAccessControlList.AuthenticatedRead);
		}

		PutObjectResult putObjectResult = s3Client.putObject(putObjectRequest);
		log.info("{} uploaded to {} bucket. Access url: {}", fileKey, s3Bucket, s3Client.getUrl(s3Bucket, fileKey));

		return result;
	}

	@Override
	public Optional<UploadResponseVO> uploadSilently(UploadRequestVO uploadRequest) {
		try {
			return Optional.of(upload(uploadRequest));
		} catch (Exception ex) {
			log.warn("Can not upload {} to s3 bucket {}.", uploadRequest, s3Bucket, ex);
			return Optional.empty();
		}
	}

	@Override
	@NotNull
	public ByteArrayOutputStream getRemoteFileContentOutputStream(String fileName, DocumentType documentType) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		S3Object s3Object = s3Client.getObject(s3Bucket, fileName);
		S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
		byte[] readBuf = new byte[1024];
		int readLen = 0;
		while ((readLen = s3ObjectInputStream.read(readBuf)) > 0) {
			byteArrayOutputStream.write(readBuf, 0, readLen);
		}
		s3ObjectInputStream.close();
		byteArrayOutputStream.close();

		return byteArrayOutputStream;
	}

}
