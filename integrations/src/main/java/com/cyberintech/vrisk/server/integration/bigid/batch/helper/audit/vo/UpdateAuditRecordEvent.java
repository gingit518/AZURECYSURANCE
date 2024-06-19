package com.cyberintech.vrisk.server.integration.bigid.batch.helper.audit.vo;

import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import lombok.Getter;

@Getter
public class UpdateAuditRecordEvent extends AuditRecordEventBase {

	private final Object oldValue;

	public UpdateAuditRecordEvent(Object source, VItemType itemType, Long itemId, Object oldValue, Object newValue, AuditLogItemId[] logItems) {
		super(source, itemType, itemId, newValue, logItems);
		this.oldValue = oldValue;
	}
}
