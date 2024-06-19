package com.cyberintech.vrisk.server.integration.bigid.client.auth;

import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClient;
import com.cyberintech.vrisk.server.integration.bigid.configuration.BigidConfigurationProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@Slf4j
public class AuthenticationClient extends BigIdClient {

	public AuthenticationClient(BigidConfigurationProperties configurationProperties) {
		super(new RestTemplate(), configurationProperties);
	}

	public AuthorizationToken getToken() {
		log.debug("Get authorization token from BigID");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(HttpHeaders.AUTHORIZATION, getConfigurationProperties().getTokenValue());

		HttpEntity<String> entity = new HttpEntity<>(null, headers);

		try {
			ResponseEntity<AuthenticationResponse> response = getRestTemplate().exchange(
				formatRequestUrl("/api/v1/refresh-access-token"),
				HttpMethod.GET, entity, AuthenticationResponse.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				AuthenticationResponse authenticationResponse = response.getBody();
				if (authenticationResponse.isSuccess()) {
					// TODO extract expiration date from the token
					log.debug("BigID authentication complete");
					return new AuthorizationToken(authenticationResponse.getSystemToken(),
						DateUtils.addDays(new Date(), 1));
				}
				log.error("BigID authentication failed. The reason is: {}", authenticationResponse.getMessage());
			}
			log.error("BigID authentication failed. Status Code is: {}", response.getStatusCodeValue());
		} catch (Exception e) {
			log.error("BigID authentication failed", e);

		}
		return null;
	}

	@Data
	@NoArgsConstructor
	private static class AuthenticationResponse {
		private boolean success;
		private String message;
		private String systemToken;
	}

}
