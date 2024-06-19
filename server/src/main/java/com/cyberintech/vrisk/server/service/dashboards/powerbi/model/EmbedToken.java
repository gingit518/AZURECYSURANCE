package com.cyberintech.vrisk.server.service.dashboards.powerbi.model;

import lombok.Data;

@Data
public class EmbedToken {
	private String token;
	private String tokenId;
	private String expiration;
}
