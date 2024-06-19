package com.cyberintech.vrisk.server.config;

import io.keen.client.java.JavaKeenClientBuilder;
import io.keen.client.java.KeenClient;
import io.keen.client.java.KeenLogging;
import io.keen.client.java.KeenProject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Slf4j
@Configuration()
@Profile("keen")
@Deprecated
public class KeenIOConfiguration {

	@Value("${keenio.project.id:}")
	private String projectId;

	@Value("${keenio.project.key.read:}")
	private String readKey;

	@Value("${keenio.project.key.write:}")
	private String writeKey;

	@Value("${keenio.debug:}")
	private String debug;

	@Value("${keenio.logging:}")
	private String logging;

	@Bean(name = "keenClient")
	public KeenClient keenClient() {
		KeenClient client = null;
		client = new JavaKeenClientBuilder().build();
		KeenClient.initialize(client);

		// Check Project ID to Define Keen.IO
		if (StringUtils.isNotEmpty(projectId)) {
			log.error("Initializing Keen.IO for project: " + projectId);

			KeenProject project = new KeenProject(projectId, writeKey, readKey);
			client.setDefaultProject(project);

			// Set Debug Mode
			if ("true".equalsIgnoreCase(debug)) {
				client.setDebugMode(true);
			}

			// Enable Loging
			if ("true".equalsIgnoreCase(logging)) {
				KeenLogging.enableLogging();
			}
		} else {
			log.error("Failed to initialize Keen.IO. Project is not defined.");
		}

		return client;
	}

}
