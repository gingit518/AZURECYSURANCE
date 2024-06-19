package com.cyberintech.vrisk.server.integration.bigid.batch.helper.audit.vo;


import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;

public class CreateAuditRecordEvent extends AuditRecordEventBase {
	public CreateAuditRecordEvent(Object source, VItemType itemType, Long itemId, Object newValue, AuditLogItemId[] logItems) {
		super(source, itemType, itemId, newValue, logItems);
	}
}
