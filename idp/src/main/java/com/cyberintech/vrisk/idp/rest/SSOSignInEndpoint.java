package com.cyberintech.vrisk.idp.rest;

import com.cyberintech.vrisk.idp.rest.dto.IdpInfoDTO;
import com.okta.jwt.AccessTokenVerifier;
import com.okta.jwt.Jwt;
import com.okta.jwt.JwtVerificationException;
import com.okta.jwt.JwtVerifiers;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;

/**
 * Removing Authorization info
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-02
 */
@RestController
@RequestMapping(
	value = SSOSignInEndpoint.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON_VALUE,
	name = "SSO Authorization"
)
@Tag(name = "SSO Authorization")
@Slf4j
public class SSOSignInEndpoint {

	static final String CONTROLLER_URI = "/sso-signin";

	@Value("${okta.jwt.issuer:}")
	private String oktaJwtIssuer;

	@Value("${okta.jwt.client-id:}")
	private String oktaJwtClientId;

	@Value("${okta.auth.app-url:}")
	private String oktaAuthAppUrl;

	@Value("${vrisk.ui.url:}")
	private String uiUrl;

	@GetMapping(value = "/info", name = "IdP Info")
	@Transactional
	public IdpInfoDTO idpInfo() {
		IdpInfoDTO result = new IdpInfoDTO();
		if (StringUtils.isNotEmpty(oktaAuthAppUrl)) {
			result.setOktaAllowed(true);
			result.setOktaAppLoginUrl(oktaAuthAppUrl);
		} else {
			result.setOktaAllowed(true);
		}

		return result;
	}

	@PostMapping(value = "/okta", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, name = "User signin")
	@Transactional
	public String oktaSignIn(HttpServletRequest request, HttpServletResponse response
		, @RequestParam MultiValueMap signInPayload
	) {

		String idToken = signInPayload.get("id_token").toString();
		idToken = idToken.replace("[", "").replace("]", "");

		AccessTokenVerifier jwtVerifier = JwtVerifiers.accessTokenVerifierBuilder()
			// .setIssuer("https://dev-47785194.okta.com/oauth2/default")
			.setIssuer(oktaJwtIssuer)
			.setAudience(oktaJwtClientId)                   // defaults to 'api://default'
			// .setAudience("api://default")                   // defaults to 'api://default'
			.setConnectionTimeout(Duration.ofSeconds(1))    // defaults to 1s
			.setRetryMaxAttempts(2)                     // defaults to 2
			.setRetryMaxElapsed(Duration.ofSeconds(10)) // defaults to 10s
			.build();

		try {
			Jwt jwt = jwtVerifier.decode(idToken);
			Map<String, Object> jwtClaims = jwt.getClaims();

			Object userName = jwtClaims.get("preferred_username");
			Object isEmailVerified = jwtClaims.get("email_verified");
			String email = (String) userName;
			Boolean emailVerified = (Boolean) isEmailVerified;

			String uiRedirectUrl = uiUrl + "/public/sign-in?granter=okta&token=" + idToken;
			response.sendRedirect(uiRedirectUrl);

			return "OK";
		} catch (JwtVerificationException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Data
	public static class OktaSignInPayload {
		private String id_token;
	}
}
