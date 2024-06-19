package com.cyberintech.vrisk.server.model.jpa.entity;

public interface IEntityWithMetadata extends IEntityWithDates {
	Users getCreatedBy();
	Users getUpdatedBy();
}
