package com.cyberintech.vrisk.server.integration.bigid.client.auth;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@AllArgsConstructor
@Slf4j
public final class BigIdAuthRequestInterceptor implements ClientHttpRequestInterceptor {

	private final String authorizationToken;

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
		throws IOException {
		if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
			if (this.authorizationToken != null) {
				request.getHeaders().add(HttpHeaders.AUTHORIZATION, this.authorizationToken);
			} else {
				log.warn("No BigID authorization token for {}", request.getURI());
			}
		}

		return execution.execute(request, body);
	}
}
