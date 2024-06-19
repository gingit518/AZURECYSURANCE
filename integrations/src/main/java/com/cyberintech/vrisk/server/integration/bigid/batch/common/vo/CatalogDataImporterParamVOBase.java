package com.cyberintech.vrisk.server.integration.bigid.batch.common.vo;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
@ToString
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class CatalogDataImporterParamVOBase {
	private final UUID id = UUID.randomUUID();
	private final Long organizationId;
	private final Long userImporterId;
}
