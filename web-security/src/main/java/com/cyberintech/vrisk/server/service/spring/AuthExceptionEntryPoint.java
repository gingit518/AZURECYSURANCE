package com.cyberintech.vrisk.server.service.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Creating Custom Exception entry point for Authentication process
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-10-27
 */
public class AuthExceptionEntryPoint implements AuthenticationEntryPoint {
	@Override
	public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {

		if (Objects.equals(httpServletRequest.getServletPath(), "/oauth/token") && Objects.equals(httpServletRequest.getContentType(), "application/json")) {
			final Map<String, Object> mapBodyException = new HashMap<>() ;

			mapBodyException.put("error"    , "Error from AuthenticationEntryPoint") ;
			mapBodyException.put("message"  , "Message from AuthenticationEntryPoint") ;
			mapBodyException.put("exception", "AuthenticationException") ;
			mapBodyException.put("path"     , httpServletRequest.getServletPath()) ;
			mapBodyException.put("timestamp", (new Date()).getTime()) ;

			httpServletResponse.setContentType("application/json") ;
			httpServletResponse.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE) ;

			final ObjectMapper mapper = new ObjectMapper() ;
			mapper.writeValue(httpServletResponse.getOutputStream(), mapBodyException) ;
		} else {
			final Map<String, Object> mapBodyException = new HashMap<>() ;

			mapBodyException.put("error"    , "Authorization") ;
			mapBodyException.put("message"  , e.getMessage()) ;
			mapBodyException.put("exception", "AuthenticationException") ;
			mapBodyException.put("path"     , httpServletRequest.getServletPath()) ;
			mapBodyException.put("timestamp", (new Date()).getTime()) ;

			httpServletResponse.setContentType("application/json") ;
			httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED) ;

			final ObjectMapper mapper = new ObjectMapper() ;
		}

	}
}
