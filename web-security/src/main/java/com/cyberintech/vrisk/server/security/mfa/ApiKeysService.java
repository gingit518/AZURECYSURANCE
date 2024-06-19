//package com.cyberintech.vrisk.server.security.mfa;
//
//import com.cyberintech.vrisk.server.model.auth.UserDetailsImpl;
//import com.cyberintech.vrisk.server.model.jpa.entity.ApiKeys;
//import com.cyberintech.vrisk.server.repository.jpa.ApiKeysRepository;
//import com.cyberintech.vrisk.server.rest.ApplicationProperties;
//import com.cyberintech.vrisk.server.security.provider.VRiskOauth2TokenServices;
//import com.cyberintech.vrisk.server.service.UserService;
//import com.cyberintech.vrisk.server.util.BeanUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.oauth2.common.OAuth2AccessToken;
//import org.springframework.security.oauth2.provider.*;
//import org.springframework.security.oauth2.provider.token.TokenStore;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.*;
//
///**
// * Api Keys Service
// *
// * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
// * @version  0.1.1
// * @since    2022-06-13
// */
//@Service
//@Slf4j
//public class ApiKeysService {
//
//	@Autowired
//	private ApplicationProperties applicationProperties;
//
//	@Autowired
//	private ApiKeysRepository apiKeysRepository;
//
//	@Lazy
//	@Autowired
//	private ClientDetailsService clientDetailsService;
//
//	/**
//	 * Get Auth token for Api Key
//	 *
//	 * @param apiKey
//	 * @return
//	 */
//	@Transactional
//	public String getSafeOauthAccessToken(String apiKey) {
//		String result = null;
//
//		Optional<ApiKeys> apiKeysOptional = apiKeysRepository.findFirstByApiKeyPublic(apiKey);
//		if (apiKeysOptional.isPresent()) {
//			VRiskOauth2TokenServices tokenServices = BeanUtil.getBean(VRiskOauth2TokenServices.class);
//			AuthenticationManager authenticationManager = BeanUtil.getBean(AuthenticationManager.class);
//			TokenStore tokenStore = BeanUtil.getBean(TokenStore.class);
//
//			String clientId = applicationProperties.getEnvironment().getProperty("application.oauth.client.id");
//			Collection<OAuth2AccessToken> activeUserTokens = tokenStore.findTokensByClientIdAndUserName(clientId, apiKeysOptional.get().getUser().getEmail());
//			Optional<OAuth2AccessToken> accessTokenOptional = activeUserTokens.stream().filter(oAuth2AccessToken -> !oAuth2AccessToken.isExpired()).findFirst();
//
//			if (accessTokenOptional.isPresent()) {
//				return accessTokenOptional.get().getValue();
//			} else {
//				// Other way authorize
//				UserDetails userDetails = UserDetailsImpl.of(apiKeysOptional.get().getUser());
//				ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
//
//				Map<String, String> requestParameters = new HashMap<>();
//				TokenRequest tokenRequest = new TokenRequest(requestParameters, clientId, Arrays.asList("read", "write"), "password");
//				OAuth2Request storedOAuth2Request = tokenRequest.createOAuth2Request(client);
//				// OAuth2Request storedOAuth2Request = oAuth2RequestFactory.createOAuth2Request(client, tokenRequest);
//
//				Authentication userAuthentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//
//				OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(storedOAuth2Request, userAuthentication);
//				OAuth2AccessToken accessToken = tokenServices.createAccessToken(oAuth2Authentication);
//
//				return accessToken.getValue();
//			}
//		}
//
//		return null;
//	}
//
//}
