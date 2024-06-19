package com.cyberintech.vrisk.server.model.jpa.domains;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class TaskLinkageTypeConverter implements AttributeConverter<TaskLinkageType, Long> {

	@Override
	public Long convertToDatabaseColumn(TaskLinkageType taskLinkageType) {
		Long taskLinkageTypeId = null;
		try {
			taskLinkageTypeId = taskLinkageType.getId();
		} catch (NullPointerException ex) { }

		return taskLinkageTypeId;
	}

	@Override
	public TaskLinkageType convertToEntityAttribute(Long taskLinkageTypeId) {
		return TaskLinkageType.of(taskLinkageTypeId);
	}
}
