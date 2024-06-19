package com.cyberintech.vrisk.server.integration.bigid.client.objectdetails.vo;

import lombok.Data;

@Data
public class ObjectDetailsVOWrapper {
	private String status;
	private int statusCode;
	private ObjectDetailsVO data;
	private String message;
}
