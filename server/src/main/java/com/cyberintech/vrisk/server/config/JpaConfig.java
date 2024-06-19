package com.cyberintech.vrisk.server.config;

import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

/**
 * Data Source and Transaction configuration.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-10-17
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
	basePackages = {"com.cyberintech.vrisk.server.repository"},
	repositoryBaseClass = CoreRepositoryImpl.class
)
@EnableJpaAuditing
@Slf4j
public class JpaConfig implements TransactionManagementConfigurer {

	/**
	 * Creating default Transaction manager
	 *
	 * @return
	 */
	@Primary
	@Bean(name = "riskqTransactionManager")
	public PlatformTransactionManager annotationDrivenTransactionManager() {
		return new JpaTransactionManager();
	}

}
