package com.cyberintech.vrisk.server.config;

import com.cyberintech.vrisk.server.security.JdbcTokenStoreImproved;
import com.cyberintech.vrisk.server.security.mfa.*;
import com.cyberintech.vrisk.server.security.provider.VRiskOauth2TokenServices;
import com.cyberintech.vrisk.server.service.spring.Oauth2AuthenticationFailureHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Application OAuth2 Security Configuration.
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 */
@Configuration
@EnableAuthorizationServer
@Order(100)
// TODO Remove this config
public class OAuth2Config extends AuthorizationServerConfigurerAdapter {

	private AuthenticationManager authenticationManager;

	@Autowired
	private AuthenticationConfiguration authConfiguration;

	/**
	 * Include DataSource dependency
	 */
	@Autowired
	private DataSource dataSource;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private MultiFactorAuthenticationService multiFactorAuthenticationService;

	private PasswordEncoder passwordEncoder;

	private ClientDetailsService clientDetailsService;

	private VRiskOauth2TokenServices oauth2TokenServices;

	@Value("${application.oauth.client.id}")
	private String oauthClientId;

	@Value("${application.oauth.client.signature}")
	private String oauthClientSecret;

	@Override
	public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
		oauthServer.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
	}

	/**
	 * Configure Client Details service
	 *
	 * @param  clients
	 * @throws Exception
	 */
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.withClientDetails(getClientDetailsService());
	}

	/**
	 * Configure Tokens managers and Endpoints
	 *
	 * @param  endpoints
	 * @throws Exception
	 */
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.authenticationManager(authenticationManager());
		endpoints.tokenStore(tokenStore());
		endpoints.userDetailsService(userDetailsService);
		endpoints.tokenGranter(tokenGranter(endpoints));
	}

	/**
	 * Create Tokens Granter for the Application
	 *
	 * @param  endpoints
	 * @return
	 * @throws Exception
	 */
	private TokenGranter tokenGranter(final AuthorizationServerEndpointsConfigurer endpoints) throws Exception {

		// Overriding Token Services for endpoints configurer
		AuthorizationServerTokenServices tokenServices = tokenServices();
		endpoints.tokenServices(tokenServices);

		// List<TokenGranter> granters = new
		// ArrayList<>(List.of(endpoints.getTokenGranter()));
		// Clear all Default Token Granters
		List<TokenGranter> granters = new ArrayList<>();
		granters.add(new PasswordTokenGranter(endpoints, authenticationManager(), multiFactorAuthenticationService));
		granters.add(new MultiFactorAuthenticationTokenGranter(endpoints, authenticationManager, multiFactorAuthenticationService));
		granters.add(new RefreshTokenGranter(endpoints.getTokenServices(), endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory()));
		granters.add(new GoogleTokenGranter(endpoints, authenticationManager, multiFactorAuthenticationService));
		granters.add(new MicrosoftTokenGranter(endpoints, authenticationManager, multiFactorAuthenticationService));
		granters.add(new OktaTokenGranter(endpoints, authenticationManager, multiFactorAuthenticationService));

		return new CompositeTokenGranter(granters);
	}

	/**
	 * @return Custom {@link AuthenticationFailureHandler} to send suitable response
	 *         to REST clients in the event of a failed authentication attempt.
	 */
	@Bean
	public AuthenticationFailureHandler authenticationFailureHandler() {
		return new Oauth2AuthenticationFailureHandler();
	}

	/**
	 * Token Store Implementation
	 *
	 * @return
	 */
	@Bean
	public TokenStore tokenStore() {
		return new JdbcTokenStoreImproved(dataSource);
	}

	/**
	 * Override Token Services to fix logic for Access/Refresh tokens
	 *
	 * @return
	 */
	@Bean
	@Primary
	public VRiskOauth2TokenServices tokenServices() {
		if (oauth2TokenServices == null) {
			VRiskOauth2TokenServices defaultTokenServices = new VRiskOauth2TokenServices();
			defaultTokenServices.setTokenStore(tokenStore());
			defaultTokenServices.setSupportRefreshToken(true);
			defaultTokenServices.setClientDetailsService(getClientDetailsService());

			oauth2TokenServices = defaultTokenServices;
		}

		return oauth2TokenServices;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		if (this.passwordEncoder == null) {
			this.passwordEncoder = new BCryptPasswordEncoder();
		}
		return this.passwordEncoder;
	}

	private ClientDetailsService getClientDetailsService() {
		if (this.clientDetailsService == null) {
			clientDetailsService = new ClientDetailsService() {
				@Override
				public ClientDetails loadClientByClientId(String s) throws ClientRegistrationException {
					BaseClientDetails clientDetails = new BaseClientDetails();

					clientDetails.setClientId(oauthClientId);
					// clientDetails.setClientSecret("{noop}21827392bacff");
					clientDetails.setClientSecret(passwordEncoder().encode(oauthClientSecret));
					clientDetails.setAccessTokenValiditySeconds(43200); // Access token to live for 12 hours
					clientDetails.setRefreshTokenValiditySeconds(2592000); // Refresh token to be valid for 30 days
					clientDetails.setScope(Arrays.asList("read", "write")); // Scope related to resource vrisk
					clientDetails.setAuthorizedGrantTypes(
						Arrays.asList("password", "refresh_token", "mfa", "google", "microsoft", "okta")); // grant types
					clientDetails.setAuthorities(
						Arrays.asList(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("MFA"))); // granted
																												// roles

					return clientDetails;
				}
			};
		}

		return clientDetailsService;
	}

	@Bean
	public AuthenticationManager authenticationManager() throws Exception {
		if (this.authenticationManager == null) {
			this.authenticationManager = authConfiguration.getAuthenticationManager();
		}
		return this.authenticationManager;
	}

}
