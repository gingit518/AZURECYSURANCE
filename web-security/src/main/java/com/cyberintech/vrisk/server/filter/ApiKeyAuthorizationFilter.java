package com.cyberintech.vrisk.server.filter;
//package com.cyberintech.vrisk.server.security.filter;
//
//import com.cyberintech.vrisk.server.http.MutableHttpServletRequest;
//import com.cyberintech.vrisk.server.security.mfa.ApiKeysService;
//import com.cyberintech.vrisk.server.util.BeanUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.*;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
///**
// * Process Authorization request to apply additional request headers
// *
// * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
// * @version  0.1.1
// * @since    2022-06-13
// */
//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE)
//@Slf4j
//public class ApiKeyAuthorizationFilter implements Filter {
//
//    /**
//     * Filter a request.
//     *
//     * @param req   the request
//     * @param res   the response
//     * @param chain the filter chain
//     * @throws IOException      throws i/o exceptions
//     * @throws ServletException throws servlet exceptions
//     */
//    @Override
//    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
//        final HttpServletRequest request = (HttpServletRequest) req;
//        final HttpServletResponse response = (HttpServletResponse) res;
//
//        String apiKeyParameter = request.getParameter("api-key");
//		if (StringUtils.isEmpty(apiKeyParameter)) {
//			apiKeyParameter = request.getHeader("api-key");
//		}
//		if (StringUtils.isNotEmpty(apiKeyParameter)) {
//			ApiKeysService apiKeysService = BeanUtil.getBean(ApiKeysService.class);
//			String accessToken = apiKeysService.getSafeOauthAccessToken(apiKeyParameter);
//
//			MutableHttpServletRequest customRequest = new MutableHttpServletRequest(request);
//			customRequest.setCustomHeader("authorization", "Bearer " + accessToken);
//			chain.doFilter(customRequest, response);
//		} else {
//			chain.doFilter(request, response);
//		}
//    }
//
//    /**
//     * Destroy the filter.
//     */
//    @Override
//    public void destroy() {
//        // nothing to destroy
//    }
//
//    /**
//     * Initialize the filter.
//     *
//     * @param filterConfig the filter configuration
//     * @throws ServletException throws servlet exceptions
//     */
//    @Override
//    public void init(final FilterConfig filterConfig) throws ServletException {
//        synchronized (log) {
//        }
//    }
//}
