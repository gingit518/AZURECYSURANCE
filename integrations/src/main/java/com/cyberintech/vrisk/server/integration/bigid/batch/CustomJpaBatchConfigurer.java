package com.cyberintech.vrisk.server.integration.bigid.batch;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.autoconfigure.batch.JpaBatchConfigurer;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
public class CustomJpaBatchConfigurer extends JpaBatchConfigurer {

	private final DataSource dataSource;
	private final BatchProperties properties;

	protected CustomJpaBatchConfigurer(BatchProperties properties, DataSource dataSource,
									   TransactionManagerCustomizers transactionManagerCustomizers,
									   EntityManagerFactory entityManagerFactory) {
		super(properties, dataSource, transactionManagerCustomizers, entityManagerFactory);
		this.dataSource = dataSource;
		this.properties = properties;
	}

	@Override
	protected JobExplorer createJobExplorer() throws Exception {
		PropertyMapper map = PropertyMapper.get();
		JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
		factory.setDataSource(this.dataSource);
		map.from(this.properties.getJdbc()::getTablePrefix).whenHasText().to(factory::setTablePrefix);
		Jackson2ExecutionContextStringSerializer defaultSerializer = getJackson2ExecutionContextStringSerializer();
		factory.setSerializer(defaultSerializer);
		factory.afterPropertiesSet();
		return factory.getObject();
	}

	@Override
	protected JobRepository createJobRepository() throws Exception {
		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();

		PropertyMapper map = PropertyMapper.get();
		map.from(() -> this.dataSource).to(factory::setDataSource);
		map.from(this::determineIsolationLevel).whenNonNull().to(factory::setIsolationLevelForCreate);
		map.from(this.properties.getJdbc()::getTablePrefix).whenHasText().to(factory::setTablePrefix);
		map.from(this::getTransactionManager).to(factory::setTransactionManager);
		Jackson2ExecutionContextStringSerializer defaultSerializer = getJackson2ExecutionContextStringSerializer();
		factory.setSerializer(defaultSerializer);
		factory.afterPropertiesSet();

		return factory.getObject();
	}

	private Jackson2ExecutionContextStringSerializer getJackson2ExecutionContextStringSerializer() {
		Jackson2ExecutionContextStringSerializer defaultSerializer = new Jackson2ExecutionContextStringSerializer();
		ObjectMapper.DefaultTypeResolverBuilder defaultTyping =
			new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
		defaultTyping.init(JsonTypeInfo.Id.CLASS, null);
		defaultTyping.inclusion(JsonTypeInfo.As.PROPERTY);
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.setDefaultTyping(defaultTyping);
		defaultSerializer.setObjectMapper(mapper);
		return defaultSerializer;
	}
}
