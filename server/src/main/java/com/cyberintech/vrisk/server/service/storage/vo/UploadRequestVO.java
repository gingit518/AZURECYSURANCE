package com.cyberintech.vrisk.server.service.storage.vo;

import com.cyberintech.vrisk.server.model.jpa.domains.DocumentType;
import lombok.*;

import java.io.InputStream;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@ToString(of = {"destinationPath", "fileName", "size"})
public class UploadRequestVO {
	private final String destinationPath;
	private final String fileName;
	private final long size;
	private final InputStream inputStream;
	private DocumentType documentType;
	private String contentType;
}
