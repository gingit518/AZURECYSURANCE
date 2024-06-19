package com.cyberintech.vrisk.idp.config;

import com.cyberintech.vrisk.server.model.config.CacheNode;
import com.cyberintech.vrisk.server.security.mfa.MFACodeTokenDTO;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.event.EventType;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.time.Duration;

/**
 * Spring Application Cache Configuration
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-01
 */
@Configuration
@EnableCaching
public class CacheConfig {

	@Value("${vrisk.distributed-cache.type:local}")
	private String cacheType;

	@Autowired
	private Environment environment;

	@Bean
	CacheManager getCacheManager() {

		CachingProvider provider = Caching.getCachingProvider();
		CacheManager cacheManager = provider.getCacheManager();

		CacheConfigurationBuilder<String, String> authorizationsCacheConfig = buildCacheConfig(String.class, String.class, 500l, 10l, 3000l);
		CacheConfigurationBuilder<String, MFACodeTokenDTO> otpCacheConfig = buildCacheConfig(String.class, MFACodeTokenDTO.class, 50000l, 100l, 300l);

		//create caches we need
		cacheManager.createCache(CacheNode.AUTHORIZATIONS, Eh107Configuration.fromEhcacheCacheConfiguration(authorizationsCacheConfig));
		cacheManager.createCache(CacheNode.OTP_CODES, Eh107Configuration.fromEhcacheCacheConfiguration(otpCacheConfig));

		return cacheManager;
	}

	/**
	 * Build Cache Configuration
	 *
	 * @param keyClass
	 * @param documentClass
	 * @param heapSize
	 * @param memoryLimitInMegabytes
	 * @param durationInSeconds
	 * @param <KEY>
	 * @param <DOCUMENT>
	 * @return
	 */
	public static <KEY, DOCUMENT> CacheConfigurationBuilder<KEY, DOCUMENT> buildCacheConfig(
		Class<KEY> keyClass, Class<DOCUMENT> documentClass, Long heapSize, Long memoryLimitInMegabytes, Long durationInSeconds
	) {
		CacheConfigurationBuilder<KEY, DOCUMENT> cacheConfigurationBuilder = CacheConfigurationBuilder
			.newCacheConfigurationBuilder(keyClass, documentClass, ResourcePoolsBuilder.heap(heapSize).offheap(memoryLimitInMegabytes, MemoryUnit.MB))
			.withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofSeconds(durationInSeconds)))
			.withDispatcherConcurrency(4);

		return cacheConfigurationBuilder;
	}

}
