package com.cyberintech.vrisk.server.integration.bigid.client.systemlocation.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SystemLocationListWrapper {
	@JsonProperty("system_locations")
	private List<SystemLocationVO> systemLocations;
}
