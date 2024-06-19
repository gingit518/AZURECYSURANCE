package com.cyberintech.vrisk.server.integration.bigid.client.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
public class AuthorizationToken implements Serializable {

	private static final long serialVersionUID = -2493429886343477037L;

	private String value;
	private Date expiredAt;

}
