package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch.DataImportItemProcessorBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.ComplianceRuleDataImporter;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.ComplianceRuleDataImporterFactory;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.vo.ComplianceRuleImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.vo.ComplianceRuleImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.exeption.CatalogDataItemProcessorValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@Slf4j
public class ComplianceRuleDataImportItemProcessor
	extends DataImportItemProcessorBase<ComplianceRuleImporterParamVO, ComplianceRuleImporterResultVO,
	ComplianceRuleDataImporter, ComplianceRuleDataImporterFactory> {

	public ComplianceRuleDataImportItemProcessor(ComplianceRuleDataImporterFactory factory) {
		super(factory);
	}

	protected void validateOrFail(ComplianceRuleImporterParamVO data) {
		if (data.getComplianceRuleVO() == null) {
			throw new CatalogDataItemProcessorValidationException("Can not process null compliance rule.");
		}

		if (StringUtils.isBlank(data.getComplianceRuleVO().getName())) {
			throw new CatalogDataItemProcessorValidationException("Can not process compliance rule with blank name.");
		}
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
