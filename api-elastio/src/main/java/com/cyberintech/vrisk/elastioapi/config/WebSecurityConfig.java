package com.cyberintech.vrisk.elastioapi.config;

import com.cyberintech.vrisk.server.filter.DownloadAuthorizationFilter;
import com.cyberintech.vrisk.server.security.oauth.LegacyAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Spring Application Security Configuration.
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 */
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@EnableWebSecurity
public class WebSecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
			.csrf().ignoringAntMatchers("/api/**")
			.and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).sessionFixation().none()
			.and()
			.addFilterBefore(new LegacyAuthenticationFilter(), BasicAuthenticationFilter.class)
			.addFilterBefore(new DownloadAuthorizationFilter(), BasicAuthenticationFilter.class)
			.authorizeRequests()
			.antMatchers(
				"/",
				"/login**",
				"/swagger**",
				"/swagger/**",
				"/webjars/springfox-swagger-ui/**",
				"/swagger-resources/**",
				"/csrf",
				"/oauth/**")
			.permitAll()
			// Allow actuator enpoints. eg: /health, /info etc.
			.and()
			.authorizeRequests().antMatchers(
				"/swagger-ui/**"
				, "/v3/api-docs/**"
				, "/actuator/**"
				, "/api/info/**"
				, "/api/anonymous/**"
			).permitAll()
			.and()
			.authorizeRequests().antMatchers(
				"/api/**"
			)
			.authenticated()
			.anyRequest().authenticated();

		return http.build();
	}

}
