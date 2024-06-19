package com.cyberintech.vrisk.server.service.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class Oauth2AuthenticationFailureHandler implements AuthenticationFailureHandler {
	@Override
	public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {

		httpServletResponse.setStatus(HttpStatus.I_AM_A_TEAPOT.value());

		Map<String, Object> data = new HashMap<>();
		data.put("timestamp", new Date());
		data.put("status",HttpStatus.I_AM_A_TEAPOT.value());
		data.put("message", "Access Denied");
		data.put("path", httpServletRequest.getRequestURL().toString());

		OutputStream out = httpServletResponse.getOutputStream();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(out, data);
		out.flush();
	}
}
