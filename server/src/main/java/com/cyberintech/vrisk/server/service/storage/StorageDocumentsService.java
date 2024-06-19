package com.cyberintech.vrisk.server.service.storage;

import com.cyberintech.vrisk.server.model.jpa.domains.DocumentType;
import com.cyberintech.vrisk.server.service.storage.vo.UploadRequestVO;
import com.cyberintech.vrisk.server.service.storage.vo.UploadResponseVO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

public interface StorageDocumentsService {

	/**
	 * Returns Storage URL for the System
	 *
	 * @return
	 */
	String getStorageURL();

	UploadResponseVO upload(UploadRequestVO uploadRequest);

	Optional<UploadResponseVO> uploadSilently(UploadRequestVO uploadRequest);

	ByteArrayOutputStream getRemoteFileContentOutputStream(String fileName, DocumentType documentType) throws IOException;

}
