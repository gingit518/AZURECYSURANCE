package com.cyberintech.vrisk.server.config;

import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepositoryImpl;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan("com.cyberintech.vrisk.server.model")
@EnableJpaRepositories(basePackages = {"com.cyberintech.vrisk.server.repository" }, repositoryBaseClass = CoreRepositoryImpl.class)
public class JpaConfig {

}
