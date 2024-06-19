package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.vo.ComplianceRuleImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.vo.ComplianceRuleImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.MDCBatchLoggingHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MDCContextComplianceRuleItemProcessingListener
	implements ItemProcessListener<ComplianceRuleImporterParamVO, ComplianceRuleImporterResultVO> {

	private final MDCBatchLoggingHelper mdcBatchLoggingHelper;

	@Override
	public void beforeProcess(ComplianceRuleImporterParamVO item) {
		mdcBatchLoggingHelper.setComplianceRuleImportData(item);
	}

	@Override
	public void afterProcess(ComplianceRuleImporterParamVO item, ComplianceRuleImporterResultVO result) {
		mdcBatchLoggingHelper.clearComplianceRuleImportData();
	}

	@Override
	public void onProcessError(ComplianceRuleImporterParamVO item, Exception e) {
		mdcBatchLoggingHelper.clearComplianceRuleImportData();
	}

}
