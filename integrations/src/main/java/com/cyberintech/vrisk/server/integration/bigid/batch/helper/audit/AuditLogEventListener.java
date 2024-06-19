package com.cyberintech.vrisk.server.integration.bigid.batch.helper.audit;

import com.cyberintech.vrisk.server.integration.bigid.batch.helper.audit.vo.CreateAuditRecordEvent;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.audit.vo.UpdateAuditRecordEvent;
import com.cyberintech.vrisk.server.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLogEventListener {
	private final AuditLogService auditLogService;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void listen(CreateAuditRecordEvent createAuditRecordEvent) {
		log.info("Processing Create Item Audit log event: {}", createAuditRecordEvent);
		auditLogService.create(
			createAuditRecordEvent.getItemType(),
			createAuditRecordEvent.getItemId(),
			createAuditRecordEvent.getNewValue(),
			createAuditRecordEvent.getLogItems());
	}

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void listen(UpdateAuditRecordEvent createAuditRecordEvent) {
		log.info("Processing Update Item Audit log event: {}", createAuditRecordEvent);
		auditLogService.update(
			createAuditRecordEvent.getItemType(),
			createAuditRecordEvent.getItemId(),
			createAuditRecordEvent.getNewValue(),
			createAuditRecordEvent.getNewValue(),
			createAuditRecordEvent.getLogItems());
	}
}
