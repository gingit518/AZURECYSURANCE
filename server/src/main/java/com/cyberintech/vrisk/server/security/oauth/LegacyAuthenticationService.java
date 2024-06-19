package com.cyberintech.vrisk.server.security.oauth;

import com.cyberintech.vrisk.server.model.jpa.entity.AccessToken;
import com.cyberintech.vrisk.server.repository.jpa.AccessTokenRepository;
import com.cyberintech.vrisk.server.rest.exception.NotAuthenticatedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Andrii Iakovenko
 * @since  2022-08-08
 */
@Service("legacyAuthenticationService")
@Slf4j
public class LegacyAuthenticationService {

	@Autowired
	private AccessTokenRepository accessTokenRepository;

	public String getUserNameByToken(String tokenValue) {
		// Token ID is MD5 hash from it's value
		String tokenId = extractTokenKey(tokenValue);

		// Find token in the repository by ID.
		Optional<AccessToken> tokenOpt = this.accessTokenRepository.findById(tokenId);
		if (tokenOpt.isEmpty()) {
			throw new NotAuthenticatedException("Access token is invalid or expired");
		}
		AccessToken accessToken = tokenOpt.get();

		try {

			ObjectMapper mapper = new ObjectMapper();
			OAuth2AccessTokenImproved accessTokenImproved = mapper.readValue(accessToken.getToken(), OAuth2AccessTokenImproved.class);
			DefaultOAuth2AccessToken oAuth2AccessToken = accessTokenImproved.toAccessToken();

			// Verify token is not expired
			if (oAuth2AccessToken.isExpired()) {
				throw new NotAuthenticatedException("Access token is expired");
			}

			return accessToken.getUserName();

		} catch (IOException e) {
			log.error("Token can't be deserialized", e);
			throw new NotAuthenticatedException("Access token is invalid");
		}
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


	@Data
	@NoArgsConstructor
	public static class OAuth2AccessTokenImproved {

		private Map<String, Object> additionalInformation;

		private Set<String> scope;

		private String refreshToken;

		private String tokenType;

		private boolean isExpired;

		private Date expiration;

		private int expiresIn;

		private String value;

		public DefaultOAuth2AccessToken toAccessToken() {
			DefaultOAuth2AccessToken result = new DefaultOAuth2AccessToken(this.getValue());
			result.setAdditionalInformation(this.getAdditionalInformation());
			result.setScope(this.getScope());
			// result.setRefreshToken(this.getRefreshToken());
			result.setTokenType(this.getTokenType());
			// result.isExpired(this.isExpired());
			result.setExpiration(this.getExpiration());
			// result.setExpiresIn(this.getExpiresIn());
			result.setValue(this.getValue());

			return result;
		}

	}

}
