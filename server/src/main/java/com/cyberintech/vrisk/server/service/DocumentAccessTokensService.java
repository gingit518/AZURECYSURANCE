package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.auth.UserDetailsImpl;
import com.cyberintech.vrisk.server.model.jpa.entity.DocumentAccessTokens;
import com.cyberintech.vrisk.server.model.jpa.entity.Documents;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.jpa.DocumentAccessTokensReporitory;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Document Access Tokens management Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-03-21
 */
@Service
@Slf4j
public class DocumentAccessTokensService {

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private DocumentAccessTokensReporitory documentAccessTokensReporitory;

	@Autowired
	DocumentService documentService;

	@Autowired
	UserService userService;

	/**
	 * Create download token for the document
	 *
	 * @param documentId
	 * @return
	 */
	public DocumentAccessTokens create(Long documentId) {
		return create(documentId, 259200l);
	}

	/**
	 * Create download token for the document
	 *
	 * @param document
	 * @return
	 */
	public DocumentAccessTokens create(Documents document) {
		return create(document.getId(), 259200l);
	}

	/**
	 * Create download token for the document
	 *
	 * @param documentId
	 * @param expiration in seconds
	 * @return
	 */
	public DocumentAccessTokens create(Long documentId, Long expiration) {

		UUID uuid = UUID.randomUUID();
		String accessTokenUid = uuid.toString();
		Users currentUser = userService.getCurrentUserEntity();

		DocumentAccessTokens entity = new DocumentAccessTokens();
		entity.setUser(currentUser);
		entity.setDocumentId(documentId);
		entity.setCode(accessTokenUid);
		entity.setActive(true);
		entity.setCreatedAt(new Date());
		entity.setExpiredAt(new Date(System.currentTimeMillis() + expiration * 1000L));

		DocumentAccessTokens result = documentAccessTokensReporitory.save(entity);

		return result;
	}

	/**
	 * Build download URL
	 *
	 * @param documentId
	 * @return
	 */
	public String buildDownloadUrl(Long documentId) {
		Documents document = documentService.getItemForCurrentOrganization(documentId);
		DocumentAccessTokens documentAccessToken = create(document);

		return getLinkUrl(documentAccessToken);
	}

	/**
	 * Get URL for reset Link
	 *
	 * @param documentAccessToken
	 * @return
	 */
	public String getLinkUrl(DocumentAccessTokens documentAccessToken) {
		String linkUrl;

		/*
		if (userService.isAuthorized() && userService.hasRole(RoleType.ADMIN)) {
			linkUrl = applicationProperties.getAdminUiUrl() + "/public/change-password/" + linkDetails.getCode();
		} else {
			linkUrl = applicationProperties.getUiUrl() + "/public/change-password/" + linkDetails.getCode();
		}
		*/
		linkUrl = applicationProperties.getApiUrl() + "/api/documents/download?dat=" + documentAccessToken.getCode();

		return linkUrl;
	}

	/**
	 * Remove expired tokens
	 *
	 * @return
	 */
	@Transactional
	public void removeExpiredTokens() {

		// TODO optimize in the future
		Date tokensExpiredTime = new Date(System.currentTimeMillis() + 86400000L);
		List<DocumentAccessTokens> tokensExpiredList = documentAccessTokensReporitory.getAllExpiredTokens(tokensExpiredTime);
		for (DocumentAccessTokens token: tokensExpiredList) {
			log.debug(MessageFormat.format("## Removing expired download token: {0}, {1}, {2}", token.getExpiredAt(), token.getUser() != null ? token.getUser().getId() : "-", token.getCode()));
			documentAccessTokensReporitory.delete(token);
		}

	}

	/**
	 * Get Auth token for Document Access token
	 *
	 * @param documentAccessToken
	 * @return
	 */
	@Transactional
	public UserDetails getUserDetailsFromDocumentAccessToken(String documentAccessToken) {
		UserDetails result = null;

		String[] tokenParts = documentAccessToken.split("\\?");
		String tokenString = tokenParts[0];
		Optional<DocumentAccessTokens> accessTokenDetails = documentAccessTokensReporitory.findFirstByCode(tokenString);

		if (accessTokenDetails.isPresent()) {
			DocumentAccessTokens documentAccessTokenDetails = accessTokenDetails.get();
			if (Boolean.TRUE.equals(documentAccessTokenDetails.getActive()) && documentAccessTokenDetails.getExpiredAt().after(new Date())) {
				result = UserDetailsImpl.of(documentAccessTokenDetails.getUser());
			}
		}

		return result;
	}

	/**
	 * Get Auth token for Document Access token
	 *
	 * @param documentAccessToken
	 * @return
	 */
	// TODO implement new way of oAuth Access tokens
	@Transactional
	public String getSafeDocumentAccessToken(String documentAccessToken) {
		String result = null;

		String[] tokenParts = documentAccessToken.split("\\?");
		String tokenString = tokenParts[0];
		Optional<DocumentAccessTokens> accessTokenDetails = documentAccessTokensReporitory.findFirstByCode(tokenString);

		/*
		VRiskOauth2TokenServices tokenServices = BeanUtil.getBean(VRiskOauth2TokenServices.class);
		AuthenticationManager authenticationManager = BeanUtil.getBean(AuthenticationManager.class);
		TokenStore tokenStore = BeanUtil.getBean(TokenStore.class);

		String clientId = applicationProperties.getEnvironment().getProperty("application.oauth.client.id");
		Collection<OAuth2AccessToken> activeUserTokens = tokenStore.findTokensByClientIdAndUserName(clientId, accessTokenDetails.get().getUser().getEmail());
		Optional<OAuth2AccessToken> accessTokenOptional = activeUserTokens.stream().filter(oAuth2AccessToken -> !oAuth2AccessToken.isExpired()).findFirst();
		// OAuth2Authentication authentication = tokenServices.loadAuthentication(accessTokenOptional.get().getValue());
		// authenticationManager.authenticate(authentication);

		result = accessTokenOptional.get().getValue();
		*/

		return result;
	}

}
