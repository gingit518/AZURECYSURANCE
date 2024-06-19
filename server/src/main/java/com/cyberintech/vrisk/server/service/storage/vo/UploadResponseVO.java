package com.cyberintech.vrisk.server.service.storage.vo;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@RequiredArgsConstructor
@ToString(of = {"url", "fileName", "size", "isPublic"})
public class UploadResponseVO {
	private String url;
	private String fileName;
	private int size;
	private Boolean isPublic = false;
}
