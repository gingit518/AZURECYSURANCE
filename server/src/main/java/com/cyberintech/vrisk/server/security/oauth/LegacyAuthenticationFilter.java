package com.cyberintech.vrisk.server.security.oauth;

import com.cyberintech.vrisk.server.model.auth.UserDetailsImpl;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import com.cyberintech.vrisk.server.util.BeanUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

public class LegacyAuthenticationFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		String tokenValue = extractTokenValue(request.getHeader("Authorization"));
		if (StringUtils.isEmpty(tokenValue)) {
			tokenValue = extractTokenValue(request.getParameter("token"));
		}

		if (tokenValue != null && !"null".equalsIgnoreCase(tokenValue)) {
			logger.debug(String.format("Legacy authentication with token %s", tokenValue));

			try {
				// validate the value in xAuth
				LegacyAuthenticationService authenticationService = BeanUtil.getBean(LegacyAuthenticationService.class);

				String username = authenticationService.getUserNameByToken(tokenValue);

				// Create our Authentication and let Spring know about it
				Authentication auth = new LegacyAuthenticationToken(null, username, getUser(username));
				auth.setAuthenticated(true);
				SecurityContextHolder.getContext().setAuthentication(auth);
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Authenticated. Token: %s", tokenValue));
				}
			} catch (Exception e) {
				if (checkIsAuthPath(request.getServletPath())) {
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("NOT Authenticated. Token: %s", tokenValue));
					}
					response.setStatus(HttpStatus.UNAUTHORIZED.value());
					return;
				}
			}
		}

		filterChain.doFilter(request, response);
	}

	private boolean checkIsAuthPath(String authPath) {
		boolean result = true;

		if (StringUtils.isNotEmpty(authPath)) {
			if (
				authPath.startsWith("/actuator")
				|| authPath.startsWith("/api/info")
			) {
				result = false;
			}
		}

		return result;
	}

	private String extractTokenValue(String headerValue) {
		if (headerValue == null) {
			return null;
		}
		return StringUtils.trim(StringUtils.removeStart(headerValue, DefaultOAuth2AccessToken.BEARER_TYPE));
	}

	private UserDetailsImpl getUser(String username) {
		UserRepository userRepository = BeanUtil.getBean(UserRepository.class);

		// Find user by email
		Optional<Users> userOpt = userRepository.findByEmailFetchRoles(username);
		if (userOpt.isEmpty()) {
			logger.debug(String.format("User '%s' doesn't exists", username));
			throw new UsernameNotFoundException("User not found");
		}
		Users user = userOpt.get();
		if (!BooleanUtils.isTrue(user.getEnabled())) {
			logger.debug(String.format("User '%s' is disabled", username));
			throw new UsernameNotFoundException("User is disabled");
		}
		return UserDetailsImpl.of(user);
	}

}
