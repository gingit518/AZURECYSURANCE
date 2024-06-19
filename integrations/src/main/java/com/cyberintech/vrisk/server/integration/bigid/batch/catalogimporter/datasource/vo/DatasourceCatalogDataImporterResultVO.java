package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.vo;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogDataImporterResultVOBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.ImportAction;
import com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo.DatasourceVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DatasourceCatalogDataImporterResultVO extends CatalogDataImporterResultVOBase {
	private final DatasourceVO datasourceVO;

	private Long technologyId;
	private Long technologyCategoryId;
	private ImportAction technologyAction;
	private ImportAction technologyCategoryAction;

	private Long technologyItOwnerId;
	private String technologyItOwnerEmail;
	private Long technologyBusinessOwnerId;
	private String technologyBusinessOwnerEmail;
	private ImportAction technologyItOwnerAction;
	private ImportAction technologyBusinessOwnerAction;

	private Long infoSecFocalId;
	private String infoSecFocalEmail;
	private ImportAction infoSecFocalAction;

	private Long parentOrganizationId;
	private String parentOrganizationName;
	private ImportAction parentOrganizationAction;

	private Long subOrganizationId;
	private String subOrganizationName;
	private ImportAction subOrganizationAction;

	public DatasourceCatalogDataImporterResultVO(Long organizationId, Long userImporterId, DatasourceVO datasourceVO) {
		super(organizationId, userImporterId);
		this.datasourceVO = datasourceVO;
	}
}
