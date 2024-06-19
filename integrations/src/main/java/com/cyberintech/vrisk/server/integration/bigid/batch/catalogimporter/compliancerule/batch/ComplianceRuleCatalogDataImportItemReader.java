package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.batch;

import static com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants.COMPLIANCE_RULE_IMPORT_STEP_NAME;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch.CatalogDataImportItemReaderBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.vo.ComplianceRuleImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.JobExecutionControlVO;
import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class ComplianceRuleCatalogDataImportItemReader
	extends CatalogDataImportItemReaderBase<ComplianceRuleImporterParamVO, JobExecutionControlVO> {
	private final BigIdClientFactory clientFactory;
	private final Long organizationId;
	private final Long userId;
	private final List<ComplianceRuleImporterParamVO> items = new LinkedList<>();
	private boolean initRequired = true;

	public ComplianceRuleCatalogDataImportItemReader(JobExecutionControlVO controlVO,
		BigIdClientFactory clientFactory,
		Long organizationId, Long userId) {
		super(controlVO);
		this.clientFactory = clientFactory;
		this.organizationId = organizationId;
		this.userId = userId;
	}

	@Override
	public ComplianceRuleImporterParamVO readItem() {
		if (initRequired) {
			clientFactory.createComplianceRuleClient(this.organizationId).getAll().stream()
				.map(cr -> new ComplianceRuleImporterParamVO(organizationId, userId, cr))
				.forEach(items::add);
			initRequired = false;
		}
		return !items.isEmpty() ? items.remove(0) : null;
	}

	@Override
	protected String getStepName() {
		return COMPLIANCE_RULE_IMPORT_STEP_NAME;
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
