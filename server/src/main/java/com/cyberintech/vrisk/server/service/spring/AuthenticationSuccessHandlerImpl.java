package com.cyberintech.vrisk.server.service.spring;

import com.cyberintech.vrisk.server.model.auth.UserDetailsImpl;
import com.cyberintech.vrisk.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

	@Autowired
	private UserService userService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
		Object securityUser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		UserDetailsImpl user = null;

		// Initialize current User Details
		if (securityUser != null && securityUser instanceof UserDetailsImpl) {
			user = (UserDetailsImpl) securityUser;
			userService.updateLastLoginDate(user.getUserId());
		}
	}
}
