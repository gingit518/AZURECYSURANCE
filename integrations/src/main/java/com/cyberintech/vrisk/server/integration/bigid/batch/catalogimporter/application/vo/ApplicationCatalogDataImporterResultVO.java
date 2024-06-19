package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.vo;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogDataImporterResultVOBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.ImportAction;
import com.cyberintech.vrisk.server.integration.bigid.client.application.vo.ApplicationVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApplicationCatalogDataImporterResultVO extends CatalogDataImporterResultVOBase {
	private final ApplicationVO applicationVO;

	private Long systemId;
	private ImportAction systemAction;

	private Long ownerId;
	private String ownerName;
	private String ownerEmail;
	private ImportAction ownerAction;

	public ApplicationCatalogDataImporterResultVO(Long organizationId, Long userImporterId,
												  ApplicationVO applicationVO) {
		super(organizationId, userImporterId);
		this.applicationVO = applicationVO;
	}

}
