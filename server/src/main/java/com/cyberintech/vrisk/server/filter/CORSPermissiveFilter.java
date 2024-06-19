package com.cyberintech.vrisk.server.filter;

import com.cyberintech.vrisk.server.context.ApplicationContextThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;

import static com.cyberintech.vrisk.server.service.LanguageConstantService.DEFAULT_LANGUAGE_LOCALE;

/**
 * Cross origin resource sharing filter. Should be added to the filter chain for REST requests to set the proper headers.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-10-26
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CORSPermissiveFilter implements Filter {

    private static String[] origins;

    private static boolean allowAll = false;

    private String origin = "*";

    /**
     * Filter a request.
     *
     * @param req   the request
     * @param res   the response
     * @param chain the filter chain
     * @throws IOException      throws i/o exceptions
     * @throws ServletException throws servlet exceptions
     */
    @Override
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

		/**
		 * Initialize Application Context
		 */
		ApplicationContextThreadLocal.ApplicationContext context = new ApplicationContextThreadLocal.ApplicationContext();
		if (StringUtils.isNotEmpty(request.getHeader("organization-id"))) {
			try {
				context.setOrganizationId(Long.parseLong(request.getHeader("organization-id")));
			} catch (NumberFormatException e) {
			}
		}
		if (StringUtils.isNotEmpty(request.getHeader("Accept-Language"))) {
			String localeString = request.getHeader("Accept-Language");

			// Check that locale header contain our custom value (not browser predefined)
			if (localeString.indexOf(',') == -1 && localeString.indexOf(';') == -1) {
				try {
					Locale locale = new Locale(localeString);
					context.setLocaleString(localeString);
					context.setLocale(locale);
				} catch (NullPointerException e) {
					Locale locale = new Locale(DEFAULT_LANGUAGE_LOCALE);
					context.setLocale(locale);
					context.setLocaleString(DEFAULT_LANGUAGE_LOCALE);
					log.warn(e.getMessage(), e);
				}
			} else {
				// If default (Browser predefined) Accept-Language came, then set
				Locale locale = new Locale(DEFAULT_LANGUAGE_LOCALE);
				context.setLocale(locale);
				context.setLocaleString(DEFAULT_LANGUAGE_LOCALE);
			}
		}
		ApplicationContextThreadLocal.setContext(context);

		final String method = request.getMethod();

        boolean allowed = false;
        String headerOrigin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", "*"/*headerOrigin*/);
        if (!allowAll && headerOrigin != null && !headerOrigin.equals("file://")) {
            URI uri;
            try {
                uri = new URI(headerOrigin);
            } catch (URISyntaxException e) {
                log.error("invalid origin header '{}'", headerOrigin);
                response.setStatus(HttpStatus.FORBIDDEN.value());
                return;
            }
            if (origins != null) {
                for (String origin : origins) {
                    log.warn("compare host {} and {}", uri.getHost(), origin);
                    if (uri.getHost().endsWith(origin)) {
                        allowed = true;
                        log.warn("matched {}", origin);
                        break;
                    }
                }
            } else {
                allowed = true;
            }
        } else {
            allowed = true;
        }
        if (!allowed) {
            log.error("disallow origin header '{}'", headerOrigin);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }

        if (log.isTraceEnabled()) {
            log.trace("allowing origin '{}'", headerOrigin);
        }


        String aclHeaders = request.getHeader("Access-Control-Request-Headers");
        if (StringUtils.isEmpty(aclHeaders)) {
            aclHeaders = HttpHeaders.CONTENT_TYPE + ", " + HttpHeaders.ACCEPT + ", Remember-Me";
        } else {
            aclHeaders += ", " + HttpHeaders.CONTENT_TYPE + ", " + HttpHeaders.ACCEPT + ", Remember-Me";
        }
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, PUT, OPTIONS, POST, DELETE, HEAD, PATCH");
        response.setHeader("Access-Control-Allow-Headers", aclHeaders);
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Expose-Headers", "Remember-Me");

        // AngularJS sends an OPTIONS request before some requests, return OK

        if ("OPTIONS".equals(method)) {
            response.setStatus(HttpStatus.OK.value());
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * Destroy the filter.
     */
    @Override
    public void destroy() {
        // nothing to destroy
    }

    /**
     * Initialize the filter.
     *
     * @param filterConfig the filter configuration
     * @throws ServletException throws servlet exceptions
     */
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        synchronized (log) {
            if (origins == null && origin != null) {
                origins = origin.split(" ");
                allowAll = Arrays.asList(origins).contains("*");
            }
        }
    }
}
