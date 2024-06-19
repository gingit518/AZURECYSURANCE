package com.cyberintech.vrisk.server.integration.bigid.batch.common.vo;

import lombok.Data;

import java.util.Set;

@Data
public class JobExecutionControlVO {
	private Set<String> enabledSteps;
}
