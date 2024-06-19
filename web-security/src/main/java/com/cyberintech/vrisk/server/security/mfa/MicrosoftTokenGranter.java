package com.cyberintech.vrisk.server.security.mfa;

import com.cyberintech.vrisk.server.model.jpa.domains.IdpType;
import org.apache.commons.lang3.StringUtils;
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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Microsoft Token Granter
 *
 * @author Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 */
public class MicrosoftTokenGranter extends AbstractTokenGranter {

	private static final String GRANT_TYPE = "microsoft";

	private final AuthenticationManager authenticationManager;

	private final MultiFactorAuthenticationService multiFactorAuthenticationService;

	public MicrosoftTokenGranter(AuthorizationServerEndpointsConfigurer endpointsConfigurer, AuthenticationManager authenticationManager, MultiFactorAuthenticationService multiFactorAuthenticationService) {
		super(endpointsConfigurer.getTokenServices(), endpointsConfigurer.getClientDetailsService(), endpointsConfigurer.getOAuth2RequestFactory(), GRANT_TYPE);
		this.authenticationManager = authenticationManager;
		this.multiFactorAuthenticationService = multiFactorAuthenticationService;
	}

	@Override
	protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
		return getGoogleOAuth2Authentication(client, tokenRequest);
	}

	@Transactional
	public OAuth2Authentication getGoogleOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
		Map<String, String> parameters = new LinkedHashMap<>(tokenRequest.getRequestParameters());
		String accessTokenString = parameters.get("accessToken");

		String microsoftUserPrincipal = null;

		// Microsoft API flow
		if (StringUtils.isNotEmpty(accessTokenString)) {
			microsoftUserPrincipal = multiFactorAuthenticationService.getMicrosoftUserIdentity(accessTokenString);
		}


		if (StringUtils.isNotEmpty(microsoftUserPrincipal)) {

			UserDetails userDetails = multiFactorAuthenticationService.getIDPUserByIdentityAndType(microsoftUserPrincipal, IdpType.MICROSOFT);

			OAuth2Request storedOAuth2Request = this.getRequestFactory().createOAuth2Request(client, tokenRequest);

			Authentication userAuthentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

			// audit log
			multiFactorAuthenticationService.auditUserMicrosoftLogin(userAuthentication);

			return new OAuth2Authentication(storedOAuth2Request, userAuthentication);

		} else {
			throw new InvalidGrantException("Could not authenticate user");
		}
	}

}
