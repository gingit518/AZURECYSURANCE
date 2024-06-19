package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.vo;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogDataImporterResultVOBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.ImportAction;
import com.cyberintech.vrisk.server.integration.bigid.client.classifier.vo.FieldClassifierVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FieldClassifierDataImporterResultVO extends CatalogDataImporterResultVOBase {

	private final FieldClassifierVO classifierVO;
	private Long fieldClassifierId;
	private ImportAction importAction;

	public FieldClassifierDataImporterResultVO(Long organizationId, Long userImporterId, FieldClassifierVO classifierVO) {
		super(organizationId, userImporterId);
		this.classifierVO = classifierVO;
	}
}
