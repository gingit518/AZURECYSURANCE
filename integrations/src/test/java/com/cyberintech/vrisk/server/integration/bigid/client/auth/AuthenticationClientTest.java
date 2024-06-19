package com.cyberintech.vrisk.server.integration.bigid.client.auth;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cyberintech.vrisk.server.integration.bigid.client.BaseClientTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

@Slf4j
public class AuthenticationClientTest extends BaseClientTest {

	protected AuthenticationClient authenticationClient;

	@BeforeEach
	void setup() {
		this.authenticationClient = new AuthenticationClient(getConfigurationProperties());
	}

	@Test
	void getToken() {
		AuthorizationToken token = this.authenticationClient.getToken();
		assertNotNull(token);

		// Token is in the future
		assertNotNull(token.getExpiredAt());
		assertTrue(token.getExpiredAt().after(new Date()));
		// Token value is not blank
		assertTrue(StringUtils.isNotBlank(token.getValue()));
		log.debug("Authorization token: {}", token.getValue());
	}

}
