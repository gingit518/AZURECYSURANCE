package com.cyberintech.vrisk.server.config;

import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.cyberintech.vrisk.server.service.storage.StorageDocumentsS3;
import com.cyberintech.vrisk.server.service.storage.StorageDocumentsService;
import com.cyberintech.vrisk.server.service.storage.StorageDocumentsServiceAzure;
import com.cyberintech.vrisk.server.util.ClientMessage;
import com.cyberintech.vrisk.server.util.ClientMessageDataBaseImpl;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.NamingConventions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.concurrent.Executors;

/**
 * Base Spring Application Configuration.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-10-17
 */
@Configuration
@ComponentScan(basePackages = "com.cyberintech")
@EnableAutoConfiguration
@EnableAsync
// @PropertySource(name = "messages", value = {"classpath:i18n/messages.properties"})
public class CoreApplicationConfig {

	private final ApplicationContext applicationContext;

	private final Environment environment;

	public CoreApplicationConfig (@Autowired ApplicationContext applicationContext, @Autowired Environment environment) {
		this.applicationContext = applicationContext;
		this.environment = environment;
	}

	/**
	 * Create base Dispatcher Servlet
	 *
	 * @return
	 */
	/*
	@Bean
	public DispatcherServlet dispatcherServlet() {
		return new DispatcherServlet();
	}
	*/

	/**
	 * Create Application Properties Bean
	 *
	 * @return
	 */
	@Bean
	public ApplicationProperties applicationProperties() {
		return new ApplicationProperties();
	}

	/**
	 * Config for rest template
	 *
	 * @param builder
	 * @return
	 */
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	/**
	 * Initialize Model Mapper
	 *
	 * @return
	 */
	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();

		modelMapper.getConfiguration()
			.setFieldMatchingEnabled(true)
			.setDeepCopyEnabled(false)
			.setAmbiguityIgnored(true)
			.setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
			.setSourceNamingConvention(NamingConventions.JAVABEANS_MUTATOR);

		return modelMapper;
	}

	/**
	 * Declare Task Executor for Async Operations
	 *
	 * @return
	 */
	@Bean
	public TaskExecutor taskExecutor () {
		return new ConcurrentTaskExecutor(Executors.newFixedThreadPool(3));
	}

	@Bean(name = "templateEngineFile")
	@Primary
	public SpringTemplateEngine templateEngineFile(){

		SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
		templateResolver.setApplicationContext(this.applicationContext);
		templateResolver.setPrefix("classpath:/templates/");
		// HTML is the default value, added here for the sake of clarity.
		templateResolver.setTemplateMode(TemplateMode.HTML);
		// Template cache is true by default. Set to false if you want
		// templates to be automatically updated when modified.
		templateResolver.setCacheable(false);

		// SpringTemplateEngine automatically applies SpringStandardDialect and
		// enables Spring's own MessageSource message resolution mechanisms.
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(templateResolver);
		// Enabling the SpringEL compiler with Spring 4.2.4 or newer can
		// speed up execution in most scenarios, but might be incompatible
		// with specific cases when expressions in one template are reused
		// across different data types, so this flag is "false" by default
		// for safer backwards compatibility.
		templateEngine.setEnableSpringELCompiler(true);
		return templateEngine;
	}

	@Bean(name = "templateEngineContent")
	public TemplateEngine templateEngineContent(){

		StringTemplateResolver templateResolver = new StringTemplateResolver();
		// HTML is the default value, added here for the sake of clarity.
		templateResolver.setTemplateMode(TemplateMode.HTML);
		// Template cache is true by default. Set to false if you want
		// templates to be automatically updated when modified.
		templateResolver.setCacheable(false);

		// SpringTemplateEngine automatically applies SpringStandardDialect and
		// enables Spring's own MessageSource message resolution mechanisms.
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(templateResolver);
		// Enabling the SpringEL compiler with Spring 4.2.4 or newer can
		// speed up execution in most scenarios, but might be incompatible
		// with specific cases when expressions in one template are reused
		// across different data types, so this flag is "false" by default
		// for safer backwards compatibility.
		templateEngine.setEnableSpringELCompiler(true);
		return templateEngine;
	}

	@Bean
	public ClientMessage clientMessage() {
		ClientMessage result = new ClientMessageDataBaseImpl();

		return result;
	}

	@Bean
	public StorageDocumentsService storageDocumentsService() {
		boolean isAzure = false;

		for (String profileName : environment.getActiveProfiles()) {
			if ("azure".equalsIgnoreCase(profileName)) {
				isAzure = true;
				break;
			}
		}

		StorageDocumentsService result = null;
		if (isAzure) {
			result = new StorageDocumentsServiceAzure();
		} else {
			result = new StorageDocumentsS3();
		}
		return result;
	}
}
