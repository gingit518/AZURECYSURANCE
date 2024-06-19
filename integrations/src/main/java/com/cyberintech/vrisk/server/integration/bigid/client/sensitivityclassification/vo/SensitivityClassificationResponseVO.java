package com.cyberintech.vrisk.server.integration.bigid.client.sensitivityclassification.vo;

import lombok.Data;

@Data
public class SensitivityClassificationResponseVO {
	private String status;
	private int statusCode;
	private SCConfigsListWrapperVO data;

}
