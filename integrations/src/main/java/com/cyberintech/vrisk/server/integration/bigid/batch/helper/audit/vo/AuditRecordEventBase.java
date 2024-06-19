package com.cyberintech.vrisk.server.integration.bigid.batch.helper.audit.vo;


import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public abstract class AuditRecordEventBase extends ApplicationEvent {
	private final VItemType itemType;
	private final Long itemId;
	private final Object newValue;
	private final AuditLogItemId[] logItems;

	public AuditRecordEventBase(Object source, VItemType itemType, Long itemId, Object newValue, AuditLogItemId[] logItems) {
		super(source);
		this.itemType = itemType;
		this.itemId = itemId;
		this.newValue = newValue;
		this.logItems = logItems;
	}
}
