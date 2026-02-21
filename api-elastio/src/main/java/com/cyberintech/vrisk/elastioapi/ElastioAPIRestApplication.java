
package com.cyberintech.vrisk.elastioapi;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

/**
 * SpringBoot Application starter class
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, JacksonAutoConfiguration.class, ErrorMvcAutoConfiguration.class})
@ComponentScan(basePackages = {"com.cyberintech"})
@EntityScan(basePackages = {"com.cyberintech.vrisk.server.model"})
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@EnableEncryptableProperties
@EnableJms
@Slf4j
public class ElastioAPIRestApplication extends SpringBootServletInitializer {

	/**
	 * SpringBoot Starter method
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		// Initialize Jasypt Encryption Details
		if (StringUtils.isEmpty(System.getProperty("jasypt.encryptor.password"))) {
			if (StringUtils.isNotEmpty(System.getenv("RISKQ_SECRET"))) {
				System.setProperty("jasypt.encryptor.password", System.getenv("RISKQ_SECRET"));
			} else {
				System.setProperty("jasypt.encryptor.password", "R@skQ873Gh78Fh");
			}
		}

		/**
		 * Initializing Spring Boot Application
		 */
		final SpringApplication springApplication = new SpringApplication(ElastioAPIRestApplication.class);

		/**
		 * Get Current Environment
		 */
		ConfigurableApplicationContext applicationContext = springApplication.run(args);
		final Environment environment = applicationContext.getEnvironment();

		showStartupMessages(environment);
	}

	/**
	 * Show startup messages for Spring Application
	 *
	 * @param environment
	 */
	private static void showStartupMessages(Environment environment) {
		String applicationHost = "localhost";

		try {
			applicationHost = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException exception) {
			;
		}

		String logoString = "\n\n\n\n" +
			" \n" +
			"     .aMMMb   dMMMMb   dMP\n" +
			"    dMP\"dMP  dMP.dMP  dMP\n" +
			"   dMMMMMP  dMMMMP\"  dMP\n" +
			"  dMP dMP  dMP      dMP  \n" +
			" dMP dMP  dMP      dMP  dM " +
			" \n" +
			" \n" +
			"\t{}, v.-{}\n" +
			"============================================================================================= \n" +
			" Startup Info:\n" +
			" Local: \thttp://127.0.0.1:{}\n" +
			" External: \thttp://{}:{}\n" +
			" Swagger: \t{}\n" +
			" Profiles: \t{}\n" +
			" Database: \t{}\n" +
			"============================================================================================= \n" +
			"\n";

		log.info(
			logoString,
			environment.getProperty("info.app.name"),
			environment.getProperty("info.app.version"),
			environment.getProperty("server.port"),
			applicationHost,
			environment.getProperty("vrisk.api.url"),
			Optional.ofNullable(environment.getProperty("vrisk.api.url")).orElse("") + "/swagger-ui.html",
			environment.getActiveProfiles(),
			environment.getProperty("spring.datasource.url")
		);
	}

	/**
	 * Spring Application Builder and Configuration
	 *
	 * @param application
	 * @return
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		SpringApplicationBuilder applicationBuilder = application.sources(ElastioAPIRestApplication.class);

		final SpringApplication springApplication = applicationBuilder.application();

		return applicationBuilder;
	}

	/**
	 * Creating Root Application context and get Application Environment
	 *
	 * @param servletContext
	 * @return
	 */
	@Override
	protected WebApplicationContext createRootApplicationContext(ServletContext servletContext) {
		WebApplicationContext webApplicationContext = super.createRootApplicationContext(servletContext);

		showStartupMessages(webApplicationContext.getEnvironment());

		return webApplicationContext;
	}
}
