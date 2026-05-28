package com.cyberintech.vrisk.server.security;

import lombok.extern.slf4j.Slf4j;

/**
 * Security profiles definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  1.0.0
 * @since    2026-01-10
 */
@Slf4j
public class SecurityProfile {

    public static final String RISKQ_PUBLIC_API_HEADER = "X-RiskQ-Public-Api-Key";
    public static final String RISKQ_SECRET_API_HEADER = "X-RiskQ-Secret-Api-Key";
    public static final String RISKQ_API_HEADER = "X-RiskQ-Api-Key";

    public static final String AUTHORIZATION_SCHEME_PUBLIC_API_KEY = "PUBLIC-API-KEY";
    public static final String AUTHORIZATION_SCHEME_SECRET_API_KEY = "SECRET-API-KEY";
    public static final String AUTHORIZATION_SCHEME_API_KEY = "API-KEY";
    public static final String AUTHORIZATION_SCHEME_JWT_BEARER = "BEARER-JWT";
    public static final String AUTHORIZATION_SCHEME_HMAC_AUTH = "HMAC-AUTH";
}
