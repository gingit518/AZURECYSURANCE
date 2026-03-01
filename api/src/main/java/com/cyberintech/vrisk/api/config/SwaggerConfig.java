package com.cyberintech.vrisk.api.config;


import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@PropertySource(name = "swagger", value = "classpath:swagger.properties")
@Profile({"documentation", "swagger"})
public class SwaggerConfig implements WebMvcConfigurer {

	private final ApplicationProperties properties;

	private String clientId = "username";

	private String clientSecret = "password";

	@Autowired
	public SwaggerConfig(ApplicationProperties properties) {
		this.properties = properties;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("swagger-ui.html")
			.addResourceLocations("classpath:/META-INF/resources/");

		registry.addResourceHandler("/webjars/**")
			.addResourceLocations("classpath:/META-INF/resources/webjars/");
	}

	/*
	@Bean
	public GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder()
			.group("riskq-public")
			.pathsToMatch("/public/**")
			.build();
	}
	*/

	/**
	 * Creates swagger configuration.
	 *
	 * @return swagger configuration docket
	 */
	@Bean
	public OpenAPI apiDocket() {

		Contact apiContact = new Contact();
		apiContact.setName("Eugene A. Kalosha");
		apiContact.setEmail("ekalosha@dfusiontech.com");
		apiContact.setUrl("https://cyberintech.com");

		return new OpenAPI()
			.servers(List.of(
				new Server().url("/").description("RiskQ API (Local)")
				, new Server().url("https://dev.app.risk-q.com/rest/") .description("RiskQ API (DEV)")
			))
			.info(
				new Info()
					.title("Cyber Innovative Tech, ValuRisQ API")
					.description("Core API documentation")
					.version(properties.getBuildVersion())
					.contact(apiContact)
			)
			.components(
				new Components()
					.addSecuritySchemes("bearer-key", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
			)
			// .apis(RequestHandlerSelectors.basePackage("com.cyberintech.vrisk.api.controller"))
			// .paths(Paths)
			//.securitySchemes(Collections.singletonList(securitySchema()))
			;
	}

	/**
	 * Creates swagger mapper config bean.
	 * Appends global MVC mapping config and should be merged in the later version.
	 */
	@Bean
	public SwaggerMapperConfig swaggerMapper() {
		return new SwaggerMapperConfig();
	}

}
