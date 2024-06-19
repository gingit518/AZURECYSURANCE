package com.cyberintech.vrisk.server.security.mfa;

import com.amazonaws.services.dlm.model.InvalidRequestException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * oAuth2 Multi Factor Authentication Token Granter
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 */
public class MultiFactorAuthenticationTokenGranter extends AbstractTokenGranter {

	private static final String GRANT_TYPE = "mfa";

	private final TokenStore tokenStore;
	private final ClientDetailsService clientDetailsService;
	private final AuthenticationManager authenticationManager;
	private final MultiFactorAuthenticationService multiFactorAuthenticationService;

	/**
	 * Basic constructor
	 *
	 * @param endpointsConfigurer
	 * @param authenticationManager
	 * @param multiFactorAuthenticationService
	 */
	public MultiFactorAuthenticationTokenGranter(AuthorizationServerEndpointsConfigurer endpointsConfigurer, AuthenticationManager authenticationManager, MultiFactorAuthenticationService multiFactorAuthenticationService) {
		super(endpointsConfigurer.getTokenServices(), endpointsConfigurer.getClientDetailsService(), endpointsConfigurer.getOAuth2RequestFactory(), GRANT_TYPE);
		this.tokenStore = endpointsConfigurer.getTokenStore();
		this.clientDetailsService = endpointsConfigurer.getClientDetailsService();
		this.authenticationManager = authenticationManager;
		this.multiFactorAuthenticationService = multiFactorAuthenticationService;
	}

	/**
	 * Override oAuth2 Authentication logic
	 *
	 * @param client
	 * @param tokenRequest
	 * @return
	 */
	@Override
	protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
		Map<String, String> parameters = new LinkedHashMap<>(tokenRequest.getRequestParameters());
		final String mfaToken = parameters.get("mfa_token");
		if (mfaToken != null) {
			OAuth2Authentication authentication = loadAuthentication(mfaToken);
			final String username = authentication.getName();
			if (parameters.containsKey("mfa_code")) {
				// int code = parseCode(parameters.get("mfa_code"));
				String codeString = parameters.get("mfa_code");
				if (multiFactorAuthenticationService.verifyCode(codeString, mfaToken)) {

					// Save User Login Audit Log
					multiFactorAuthenticationService.auditUserPasswordLogin(authentication);

					return getAuthentication(tokenRequest, authentication);
				}
			} else {
				throw new InvalidRequestException("Missing Authentication code. Please specify code and try again!");
			}

			// Default is to throw exception about MFA Token
			throw new InvalidGrantException("Invalid Authentication code.");
		} else {
			throw new InvalidRequestException("Missing Authentication token. Please contact Administration.");
		}
	}

	/**
	 * Load Authentication for Access Token Value
	 *
	 * @param accessTokenValue
	 * @return
	 */
	private OAuth2Authentication loadAuthentication(String accessTokenValue) {
		OAuth2AccessToken accessToken = this.tokenStore.readAccessToken(accessTokenValue);
		if (accessToken == null) {
			throw new InvalidTokenException("Invalid access token: " + accessTokenValue);
		} else if (accessToken.isExpired()) {
			this.tokenStore.removeAccessToken(accessToken);
			throw new InvalidTokenException("Access token expired: " + accessTokenValue);
		} else {
			OAuth2Authentication result = this.tokenStore.readAuthentication(accessToken);
			if (result == null) {
				throw new InvalidTokenException("Invalid access token: " + accessTokenValue);
			}
			return result;
		}
	}

	/**
	 * Get Authentication for Token Request and oAuth Authentication object
	 *
	 * @param tokenRequest
	 * @param authentication
	 * @return
	 */
	private OAuth2Authentication getAuthentication(TokenRequest tokenRequest, OAuth2Authentication authentication) {
		Authentication user = authenticationManager.authenticate(authentication.getUserAuthentication());
		Object details = authentication.getDetails();
		authentication = new OAuth2Authentication(authentication.getOAuth2Request(), user);
		authentication.setDetails(details);

		String clientId = authentication.getOAuth2Request().getClientId();
		if (clientId != null && clientId.equals(tokenRequest.getClientId())) {
			if (this.clientDetailsService != null) {
				try {
					this.clientDetailsService.loadClientByClientId(clientId);
				} catch (ClientRegistrationException e) {
					throw new InvalidTokenException("Client not valid: " + clientId, e);
				}
			}
			return refreshAuthentication(authentication, tokenRequest);
		} else {
			throw new InvalidGrantException("Client is missing or does not correspond to the MFA token");
		}
	}

	/**
	 * Refresh Authentication
	 *
	 * @param authentication
	 * @param request
	 * @return
	 */
	private OAuth2Authentication refreshAuthentication(OAuth2Authentication authentication, TokenRequest request) {
		Set<String> scope = request.getScope();
		OAuth2Request clientAuth = authentication.getOAuth2Request().refresh(request);
		if (scope != null && !scope.isEmpty()) {
			Set<String> originalScope = clientAuth.getScope();
			if (originalScope == null || !originalScope.containsAll(scope)) {
				throw new InvalidScopeException("Unable to narrow the scope of the client authentication to " + scope + ".", originalScope);
			}

			clientAuth = clientAuth.narrowScope(scope);
		}
		return new OAuth2Authentication(clientAuth, authentication.getUserAuthentication());
	}

}
