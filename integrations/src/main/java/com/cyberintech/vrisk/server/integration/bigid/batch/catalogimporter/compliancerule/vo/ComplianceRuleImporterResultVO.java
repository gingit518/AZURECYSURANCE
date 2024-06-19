package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.vo;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogDataImporterResultVOBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.ImportAction;
import com.cyberintech.vrisk.server.integration.bigid.client.compliancerule.vo.ComplianceRuleVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ComplianceRuleImporterResultVO extends CatalogDataImporterResultVOBase {

	private final ComplianceRuleVO complianceRuleVO;

	private ImportAction dataTypeClassificationAction;
	private Long dataTypeClassificationId;

	private ImportAction ownerAction;
	private Long ownerId;

	public ComplianceRuleImporterResultVO(Long organizationId, Long userImporterId, ComplianceRuleVO complianceRuleVO) {
		super(organizationId, userImporterId);
		this.complianceRuleVO = complianceRuleVO;
	}
}
