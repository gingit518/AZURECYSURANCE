package com.cyberintech.vrisk.server.security;

import com.cyberintech.vrisk.server.model.auth.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of Jdbc Token Store which cleaning Duplicated Access Tokens
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-03-29
 * @implNote the reason of implementation is that by unknown reason Spring at some point duplicates access token
 * 			 and then fails through stack:
 * 			 JdbcTokenStore.getAccessToken -> jdbcTemplate.queryForObject -> DataAccessUtils.nullableSingleResult -> IncorrectResultSizeDataAccessException
 * 			 the source of duplication of the access token has not yet been found
 */
public class JdbcTokenStoreImproved extends JdbcTokenStore {

	protected static final String DUPLICATED_ACCESS_TOKENS_DELETE_STATEMENT = "delete from oauth_access_token where client_id = ? AND user_name = ? ";

	private String deleteDuplicatedAccessTokensSql = DUPLICATED_ACCESS_TOKENS_DELETE_STATEMENT;

	protected JdbcTemplate jdbcTemplateOwn;

	protected ObjectMapper mapper;

	public JdbcTokenStoreImproved(DataSource dataSource) {
		super(dataSource);

		Assert.notNull(dataSource, "DataSource required");
		this.jdbcTemplateOwn = new JdbcTemplate(dataSource);

		this.mapper = new ObjectMapper();
	}

	public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
		try {
			return super.getAccessToken(authentication);
		} catch (IncorrectResultSizeDataAccessException duplicationException) {
			String clientId = authentication.getOAuth2Request().getClientId();
			String userName = ((UserDetailsImpl) authentication.getUserAuthentication().getPrincipal()).getUsername();

			this.jdbcTemplateOwn.update(deleteDuplicatedAccessTokensSql, clientId, userName);

			return null;
		}
	}

	protected byte[] serializeAccessToken(OAuth2AccessToken token) {
		try {
			OAuth2AccessTokenImproved accessToken = OAuth2AccessTokenImproved.of(token);
			String result = mapper.writeValueAsString(accessToken);

			return result.getBytes(StandardCharsets.UTF_8);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		// return SerializationUtils.serialize(token);
	}

	protected OAuth2AccessToken deserializeAccessToken(byte[] token) {
		String json = new String(token, StandardCharsets.UTF_8);

		try {
			OAuth2AccessTokenImproved accessToken = mapper.readValue(token, OAuth2AccessTokenImproved.class);
			OAuth2AccessToken result = accessToken.toAccessToken();

			return result;
		} catch (IOException e) {
			// throw new RuntimeException(e);
			return null;
		}
		// return (OAuth2AccessToken)SerializationUtils.deserialize(token);
	}

	@Data
	@NoArgsConstructor
	public static class OAuth2AccessTokenImproved {

		private Map<String, Object> additionalInformation;

		private Set<String> scope;

		private DefaultOAuth2RefreshToken refreshToken;

		private String tokenType;

		private boolean isExpired;

		private Date expiration;

		private int expiresIn;

		private String value;

		public OAuth2AccessToken toAccessToken() {
			DefaultOAuth2AccessToken result = new DefaultOAuth2AccessToken(this.getValue());
			result.setAdditionalInformation(this.getAdditionalInformation());
			result.setScope(this.getScope());
			result.setRefreshToken(this.getRefreshToken());
			result.setTokenType(this.getTokenType());
			// result.isExpired(this.isExpired());
			result.setExpiration(this.getExpiration());
			// result.setExpiresIn(this.getExpiresIn());
			result.setValue(this.getValue());

			return result;
		}

		public static OAuth2AccessTokenImproved of(OAuth2AccessToken accessToken) {
			OAuth2AccessTokenImproved result = new OAuth2AccessTokenImproved();
			result.setAdditionalInformation(accessToken.getAdditionalInformation());
			result.setScope(accessToken.getScope());
			if (accessToken.getRefreshToken() != null) result.setRefreshToken(new DefaultOAuth2RefreshToken(accessToken.getRefreshToken().getValue()));
			result.setTokenType(accessToken.getTokenType());
			result.setExpired(accessToken.isExpired());
			result.setExpiration(accessToken.getExpiration());
			result.setExpiresIn(accessToken.getExpiresIn());
			result.setValue(accessToken.getValue());

			return result;
		}
	}

}
