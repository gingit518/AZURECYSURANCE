package com.cyberintech.vrisk.idp.config;

import com.cyberintech.vrisk.server.filter.CORSPermissiveFilter;
import com.cyberintech.vrisk.server.filter.JsonToUrlEncodedAuthenticationFilter;
import com.cyberintech.vrisk.server.service.spring.AuthExceptionEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

/**
 * @author Andrii Iakovenko
 * @since  2022-07-25
 */
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	JsonToUrlEncodedAuthenticationFilter jsonFilter;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		//@formatter:off
		http
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
			// Adding Permissive CORS Filter to allow JS cross origin
			.addFilterBefore(new CORSPermissiveFilter(), ChannelProcessingFilter.class)
//			.addFilterBefore(new DownloadAuthorizationFilter(), AbstractPreAuthenticatedProcessingFilter.class)
//			.addFilterAfter(new ApiKeyAuthorizationFilter(), DownloadAuthorizationFilter.class)
			.addFilterAfter(jsonFilter, BasicAuthenticationFilter.class)
			.exceptionHandling().authenticationEntryPoint(new AuthExceptionEntryPoint())
			.and()
			.antMatcher("/api/**")
			.authorizeRequests()
			.antMatchers(
				"/",
				"/login**",
				"/swagger**",
				"/swagger/**",
				"/webjars/springfox-swagger-ui/**",
				"/swagger-resources/**",
				"/csrf",
				"/oauth/**"
			)
			.permitAll()
			.antMatchers("/actuator/**", "/api/info/**", "/api/anonymous/**").permitAll()
			.and()
			.authorizeRequests(authorizeRequests -> authorizeRequests.anyRequest().authenticated())
			.formLogin().disable();
		//@formatter:on
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
