package com.cyberintech.vrisk.server.filter;

import com.cyberintech.vrisk.server.http.MutableHttpServletRequest;
import com.cyberintech.vrisk.server.security.oauth.LegacyAuthenticationToken;
import com.cyberintech.vrisk.server.service.DocumentAccessTokensService;
import com.cyberintech.vrisk.server.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Process Authorization request to apply additional request headers
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-04-04
 */
@Slf4j
public class DownloadAuthorizationFilter extends OncePerRequestFilter {

    /**
     * Filter a request.
     *
     * @param request   the request
     * @param response   the response
     * @param filterChain the filter chain
     * @throws IOException      throws i/o exceptions
     * @throws ServletException throws servlet exceptions
     */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		final String method = request.getMethod();

		String downloadAuthorizationToken = request.getParameter("dat");
		if (StringUtils.isNotEmpty(downloadAuthorizationToken)
			&&
			(
				request.getServletPath().indexOf("/download") != -1
					|| request.getServletPath().indexOf("/data-export") != -1
					|| request.getServletPath().indexOf("/csv/export") != -1
			)
		) {
			DocumentAccessTokensService documentAccessTokensService = BeanUtil.getBean(DocumentAccessTokensService.class);
			UserDetails accessUser = documentAccessTokensService.getUserDetailsFromDocumentAccessToken(downloadAuthorizationToken);

			if (accessUser != null) {
				// Create our Authentication and let Spring know about it
				Authentication auth = new LegacyAuthenticationToken(accessUser.getAuthorities(), accessUser.getUsername(), accessUser);
				auth.setAuthenticated(true);
				SecurityContextHolder.getContext().setAuthentication(auth);

				log.debug(String.format("Authenticated. Download Token: %s", downloadAuthorizationToken));
			}

			filterChain.doFilter(request, response);
		} else {
			filterChain.doFilter(request, response);
		}
	}

	/**
     * Destroy the filter.
     */
    @Override
    public void destroy() {
        // nothing to destroy
    }

}
