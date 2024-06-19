package com.cyberintech.vrisk.server.security.mfa;

import com.cyberintech.vrisk.server.model.jpa.domains.IdpType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.util.BeanUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.okta.jwt.AccessTokenVerifier;
import com.okta.jwt.Jwt;
import com.okta.jwt.JwtVerificationException;
import com.okta.jwt.JwtVerifiers;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Okta Token Granter
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 */
public class OktaTokenGranter extends AbstractTokenGranter {

	private static final String GRANT_TYPE = "okta";

	private final AuthenticationManager authenticationManager;

	private final MultiFactorAuthenticationService multiFactorAuthenticationService;

	public OktaTokenGranter(AuthorizationServerEndpointsConfigurer endpointsConfigurer, AuthenticationManager authenticationManager, MultiFactorAuthenticationService multiFactorAuthenticationService) {
		super(endpointsConfigurer.getTokenServices(), endpointsConfigurer.getClientDetailsService(), endpointsConfigurer.getOAuth2RequestFactory(), GRANT_TYPE);
		this.authenticationManager = authenticationManager;
		this.multiFactorAuthenticationService = multiFactorAuthenticationService;
	}

	@Override
	protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
		return getOktaOAuth2Authentication(client, tokenRequest);
	}

	@Transactional
	public OAuth2Authentication getOktaOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
		Map<String, String> parameters = new LinkedHashMap<>(tokenRequest.getRequestParameters());
		String idTokenString = parameters.get("token");

		Environment environment = BeanUtil.getBean(Environment.class);
		String oktaJwtIssuer = environment.getProperty("okta.jwt.issuer");
		String oktaJwtClientId = environment.getProperty("okta.jwt.client-id");

		AccessTokenVerifier jwtVerifier = JwtVerifiers.accessTokenVerifierBuilder()
			// .setIssuer("https://dev-47785194.okta.com/oauth2/default")
			.setIssuer(oktaJwtIssuer)
			.setAudience(oktaJwtClientId)                   // defaults to 'api://default'
			// .setAudience("api://default")                   // defaults to 'api://default'
			.setConnectionTimeout(Duration.ofSeconds(5))    // defaults to 1s
			.setRetryMaxAttempts(3)                     // defaults to 2
			.setRetryMaxElapsed(Duration.ofSeconds(10)) // defaults to 10s
			.build();

		try {
			Jwt jwt = jwtVerifier.decode(idTokenString);
			Map<String, Object> jwtClaims = jwt.getClaims();
			Object userName = jwtClaims.get("preferred_username");
			Object isEmailVerified = jwtClaims.get("email_verified");

			if (userName != null) {

				// Get profile information from payload
				String email = (String) userName;
				Boolean emailVerified = (Boolean) isEmailVerified;

				if(StringUtils.isEmpty(email) && !emailVerified) {
					throw new ForbiddenException(MessageFormat.format("Google Account email ({0}) is not verified.", email));
				}
				UserDetails userDetails = multiFactorAuthenticationService.getIDPUserByIdentityAndType(email, IdpType.OKTA);

				OAuth2Request storedOAuth2Request = this.getRequestFactory().createOAuth2Request(client, tokenRequest);

				Authentication userAuthentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

				// audit log
				multiFactorAuthenticationService.auditUserAuthEvent(userAuthentication, VItemType.USER_OKTA_LOGIN);

				return new OAuth2Authentication(storedOAuth2Request, userAuthentication);

			} else {
				throw new InvalidGrantException("Could not authenticate user by given token: " + idTokenString);
			}

		} catch (JwtVerificationException e) {
			throw new RuntimeException(e);
		}

	}

}
