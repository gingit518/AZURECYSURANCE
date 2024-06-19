package com.cyberintech.vrisk.server.integration.bigid.client.identitylocation.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class IdentityLocationListWrapper {
	@JsonProperty("identity_locations")
	private List<IdentityLocationVO> identityLocations;
}
