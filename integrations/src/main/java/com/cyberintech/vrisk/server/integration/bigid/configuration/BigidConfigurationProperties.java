package com.cyberintech.vrisk.server.integration.bigid.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BigidConfigurationProperties implements Serializable {

	private static final long serialVersionUID = 8004372795610845055L;

	private String baseServiceUrl;
	private String tokenName;
	private String tokenValue;
}
