package com.cyberintech.vrisk.server.integration.bigid.client.objectdetails.vo.application;

import com.cyberintech.vrisk.server.integration.bigid.client.objectdetails.vo.ObjectDetailsVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ObjectDetailsApplicationsAwareVO extends ObjectDetailsVO {
	@JsonProperty("application_name")
	private List<String> applications;
}
