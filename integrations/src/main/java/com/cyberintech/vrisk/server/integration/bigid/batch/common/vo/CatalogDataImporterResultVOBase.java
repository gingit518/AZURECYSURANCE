package com.cyberintech.vrisk.server.integration.bigid.batch.common.vo;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class CatalogDataImporterResultVOBase {
	private final UUID id = UUID.randomUUID();
	private final Long organizationId;
	private final Long userImporterId;
	private CatalogDataImporterStatus status = CatalogDataImporterStatus.SUCCESS;
	private String errorMessage = StringUtils.EMPTY;
}
