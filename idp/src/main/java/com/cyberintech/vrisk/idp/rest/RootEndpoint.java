package com.cyberintech.vrisk.idp.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

/**
 * Default IdP controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2023-02-09
 */
@RestController
@Tag(name = "Default Application endpoint")
@Slf4j
public class RootEndpoint {

	@GetMapping("/")
	String sayHello(@AuthenticationPrincipal OidcUser oidcUser) {
		return "Hello: " + oidcUser.getFullName();
	}
}
