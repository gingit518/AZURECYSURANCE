package com.cyberintech.vrisk.idp.rest;

import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AccessToken;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.AccessTokenRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import com.cyberintech.vrisk.server.security.mfa.MultiFactorAuthenticationService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * Removing Authorization info
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-02
 */
@RestController
@RequestMapping(
	value = RevokeTokenEndpoint.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Authorization and Logout"
)
@Tag(name = "Authorization and Logout")
@Slf4j
public class RevokeTokenEndpoint {

	static final String CONTROLLER_URI = "/logout";

	@Autowired
	TokenStore tokenStore;

	@Autowired
	MultiFactorAuthenticationService multiFactorAuthenticationService;

	@Autowired
	AccessTokenRepository accessTokenRepository;

	@Autowired
	UserRepository userRepository;

	@RequestMapping(method = RequestMethod.GET, value = "", name = "User logout")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@Transactional
	public String revokeToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if (authorization != null && authorization.contains("Bearer")){
			String tokenId = authorization.substring("Bearer".length() + 1);

			OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenId);

			// Audit Logout Event
			try {
				String accessTokenId = extractTokenKey(accessToken.getValue());
				Optional<AccessToken> accessTokenDetails = accessTokenRepository.findById(accessTokenId);
				if (accessTokenDetails.isPresent()) {
					Optional<Users> currentUser = userRepository.findFirstByEmailIgnoreCase(accessTokenDetails.get().getUserName());
					if (currentUser.isPresent()) {
						multiFactorAuthenticationService.auditUserAuthEvent(currentUser.get().getId(), VItemType.USER_LOGOUT);
					}
				}
			} catch (Exception exception) {
				log.error("Failed to process Audit for Logout Token", exception);
			}

			if (accessToken != null && accessToken.getRefreshToken() != null) {
				String refreshTokenId = accessToken.getRefreshToken().getValue();

				OAuth2RefreshToken refreshToken = tokenStore.readRefreshToken(refreshTokenId);
				if (refreshToken != null) {
					tokenStore.removeRefreshToken(refreshToken);
				}

				tokenStore.removeAccessToken(accessToken);
			}
		}

		return "\"OK\"";
	}

	protected String extractTokenKey(String value) {
		if (value == null) {
			return null;
		}
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
		}

		try {
			byte[] bytes = digest.digest(value.getBytes("UTF-8"));
			return String.format("%032x", new BigInteger(1, bytes));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).");
		}
	}

}
