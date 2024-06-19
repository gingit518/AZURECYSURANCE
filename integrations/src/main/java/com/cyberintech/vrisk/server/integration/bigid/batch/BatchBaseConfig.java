package com.cyberintech.vrisk.server.integration.bigid.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class BatchBaseConfig {
	
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
}
