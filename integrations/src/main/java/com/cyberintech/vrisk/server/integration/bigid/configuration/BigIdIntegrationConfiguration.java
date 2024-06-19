package com.cyberintech.vrisk.server.integration.bigid.configuration;
/*
import com.cyberintech.vrisk.server.integration.bigid.client.auth.BigIdClientAuthenticationHandler;
import com.cyberintech.vrisk.server.integration.bigid.client.auth.BigIdOauth2RequestAuthenticator;
import com.cyberintech.vrisk.server.integration.bigid.configuration.oauth2.resource.BigIdResourceDetails;
import com.cyberintech.vrisk.server.integration.bigid.configuration.oauth2.token.BigIdAccessTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(BigidConfigurationProperties.class)
@AllArgsConstructor
public class BigIdIntegrationConfiguration {

	private final BigidConfigurationProperties bigidConfigurationProperties;

	@Bean
	public RestTemplate bigIdRestTemplate(AccessTokenProvider bigIdAccessTokenProvider) {
		OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(bigIdOauth2ProtectedResourceDetails(), new DefaultOAuth2ClientContext());
		oAuth2RestTemplate.setAccessTokenProvider(bigIdAccessTokenProvider);
		oAuth2RestTemplate.setAuthenticator(new BigIdOauth2RequestAuthenticator());
		oAuth2RestTemplate.setErrorHandler(new DefaultResponseErrorHandler());
		return oAuth2RestTemplate;
	}

	@Bean
	public AccessTokenProvider bigIdAccessTokenProvider(MappingJackson2HttpMessageConverter messageConverter) {
		BigIdAccessTokenProvider bigIdAccessTokenProvider = new BigIdAccessTokenProvider(messageConverter);
		bigIdAccessTokenProvider.setAuthenticationHandler(new BigIdClientAuthenticationHandler());
		return bigIdAccessTokenProvider;
	}

	private BigIdResourceDetails bigIdOauth2ProtectedResourceDetails() {
		BigIdResourceDetails bigIdResourceDetails = new BigIdResourceDetails(
			bigidConfigurationProperties.getAuthorizationUsername(),
			bigidConfigurationProperties.getAuthorizationPassword()
		);
		bigIdResourceDetails.setAccessTokenUri(String.format("%s/api/v1/sessions",
			bigidConfigurationProperties.getBaseServiceUrl()));
		return bigIdResourceDetails;
	}
}
*/