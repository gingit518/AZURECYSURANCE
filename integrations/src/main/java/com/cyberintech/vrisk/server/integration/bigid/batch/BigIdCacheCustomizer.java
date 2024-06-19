package com.cyberintech.vrisk.server.integration.bigid.batch;

import com.cyberintech.vrisk.server.integration.bigid.client.auth.AuthorizationToken;
import com.cyberintech.vrisk.server.integration.bigid.configuration.BigidConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.cache.CacheManager;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class BigIdCacheCustomizer implements CacheManagerCustomizer<ConcurrentMapCacheManager> {

	public static final String ACCESS_TOKEN_CACHE = "bigid_access_tokens";

	public static final String CONFIGURATION_PROPERTIES_CACHE = "bigid_configuration_properties";

	@Autowired
	private CacheManager cacheManager;

	@PostConstruct
	public void init() {
		CacheConfigurationBuilder<Long, AuthorizationToken> authTokenCacheConfigurationBuilder = CacheConfigurationBuilder
			.newCacheConfigurationBuilder(Long.class, AuthorizationToken.class,
				ResourcePoolsBuilder.heap(100).offheap(1, MemoryUnit.MB))
			.withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(10)))
			.withDispatcherConcurrency(4);
		cacheManager.createCache(ACCESS_TOKEN_CACHE,
			Eh107Configuration.fromEhcacheCacheConfiguration(authTokenCacheConfigurationBuilder));

		CacheConfigurationBuilder<Long, BigidConfigurationProperties> cacheConfigurationBuilder = CacheConfigurationBuilder
			.newCacheConfigurationBuilder(Long.class, BigidConfigurationProperties.class,
				ResourcePoolsBuilder.heap(100).offheap(1, MemoryUnit.MB))
			.withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(10)))
			.withDispatcherConcurrency(4);
		cacheManager.createCache(CONFIGURATION_PROPERTIES_CACHE,
			Eh107Configuration.fromEhcacheCacheConfiguration(cacheConfigurationBuilder.build()));
	}

	@Override
	public void customize(ConcurrentMapCacheManager cacheManager) {
		List<String> cacheNames = Arrays.asList(ACCESS_TOKEN_CACHE, CONFIGURATION_PROPERTIES_CACHE);
		cacheManager.setCacheNames(cacheNames);
		log.info("Registered cache names: {}", cacheNames);
	}

}
