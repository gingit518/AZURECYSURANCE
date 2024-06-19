package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.vo;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogDataImporterParamVOBase;
import com.cyberintech.vrisk.server.integration.bigid.client.compliancerule.vo.ComplianceRuleVO;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ComplianceRuleImporterParamVO extends CatalogDataImporterParamVOBase {

	private final ComplianceRuleVO complianceRuleVO;

	public ComplianceRuleImporterParamVO(Long organizationId, Long userImporterId,
										 ComplianceRuleVO complianceRuleVO) {
		super(organizationId, userImporterId);
		this.complianceRuleVO = complianceRuleVO;
	}
}
