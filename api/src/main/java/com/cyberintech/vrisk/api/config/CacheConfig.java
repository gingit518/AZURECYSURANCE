package com.cyberintech.vrisk.api.config;

import com.cyberintech.vrisk.server.model.config.CacheNode;
import com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto.OrganizationZoomInfoExtendedDetails;
import com.google.common.cache.CacheBuilder;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.event.EventType;
import org.ehcache.jsr107.Eh107Configuration;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

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

	@Bean("inMemoryCacheManager")
	public org.springframework.cache.CacheManager inMemoryCacheManager() {
		return new ConcurrentMapCacheManager() {
			@NotNull
			@Override
			protected Cache createConcurrentMapCache(@NotNull String name) {
				return new ConcurrentMapCache(name, CacheBuilder.newBuilder().expireAfterWrite(900, TimeUnit.SECONDS).build().asMap(), false);
			}
		};
	}

	@Bean
	CacheManager getCacheManager() {

		CachingProvider provider = Caching.getCachingProvider();
		CacheManager cacheManager = provider.getCacheManager();

		CacheConfigurationBuilder<String, String> authorizationsCacheConfig = buildCacheConfig(String.class, String.class, 500l, 10l, 3000l);
		CacheConfigurationBuilder<String, String> dashboardSearchCacheConfig = buildCacheConfig(String.class, String.class, 50000l, 100l, 36000l);
		CacheConfigurationBuilder<String, String> downloadLinkCacheConfig = buildCacheConfig(String.class, String.class, 50000l, 100l, 3600l);
		// CacheConfigurationBuilder<String, MFACodeTokenDTO> otpCacheConfig = buildCacheConfig(String.class, MFACodeTokenDTO.class, 50000l, 100l, 300l);
		CacheConfigurationBuilder<String, OrganizationZoomInfoExtendedDetails> zoomInfoCacheConfig = buildCacheConfig(String.class, OrganizationZoomInfoExtendedDetails.class, 100l, 100l, 86400l);

		// Create cache Asynchronous listener
		CacheEventLogger cacheEventsLogger = new CacheEventLogger();
		CacheEventListenerConfigurationBuilder asynchronousListener = CacheEventListenerConfigurationBuilder
			.newEventListenerConfiguration(cacheEventsLogger, EventType.CREATED, EventType.EXPIRED).unordered().asynchronous();

		//create caches we need
		cacheManager.createCache(CacheNode.AUTHORIZATIONS, Eh107Configuration.fromEhcacheCacheConfiguration(authorizationsCacheConfig.withService(asynchronousListener)));
		cacheManager.createCache(CacheNode.DASHBOARD_SEARCH, Eh107Configuration.fromEhcacheCacheConfiguration(dashboardSearchCacheConfig.withService(asynchronousListener)));
		cacheManager.createCache(CacheNode.DOWNLOAD_LINK, Eh107Configuration.fromEhcacheCacheConfiguration(downloadLinkCacheConfig.withService(asynchronousListener)));
		// cacheManager.createCache(CacheNode.OTP_CODES, Eh107Configuration.fromEhcacheCacheConfiguration(otpCacheConfig.withService(asynchronousListener)));
		cacheManager.createCache(CacheNode.ZOOM_INFO, Eh107Configuration.fromEhcacheCacheConfiguration(zoomInfoCacheConfig.withService(asynchronousListener)));

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

	/**
	 * Create cache manager
	 *
	 * @return
	 */
	/*
	@Bean
	public CacheManager cacheManager() {
		CacheManager cacheManager = null;

		if ("ignite".equalsIgnoreCase(cacheType)) {
			SpringCacheManager cacheManagerIgnite = new SpringCacheManager();
			cacheManagerIgnite.setConfiguration(igniteConfiguration());
			cacheManager = cacheManagerIgnite;
		} else {
			EhCacheManagerFactoryBean ehCacheManagerFactoryBean = new EhCacheManagerFactoryBean();
			ehCacheManagerFactoryBean.setConfigLocation(new ClassPathResource("ehcache.xml"));
			ehCacheManagerFactoryBean.setShared(true);

			EhCacheCacheManager ehCacheCacheManager = new EhCacheCacheManager(ehCacheManagerFactoryBean.getObject());
			cacheManager = ehCacheCacheManager;
		}

		return cacheManager;
	}
	*/

	/**
	 * Create Basic Ignite Configuration
	 * @return
	 */
//	@Bean(name = "igniteConfiguration")
//	public IgniteConfiguration igniteConfiguration() {
//
//		String igniteHost = environment.getProperty("vrisk.ignite.host", "127.0.0.1");
//
//		IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
//		igniteConfiguration.setIgniteInstanceName("vrisk");
//		// igniteConfiguration.setPeerClassLoadingEnabled(true);
//		igniteConfiguration.setLocalHost(igniteHost);
//
//		TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
//		TcpDiscoveryMulticastIpFinder multicastIpFinder = new TcpDiscoveryMulticastIpFinder();
//		multicastIpFinder.setAddresses(Collections.singletonList(igniteHost + ":47500..47509"));
//		tcpDiscoverySpi.setIpFinder(multicastIpFinder);
//		tcpDiscoverySpi.setLocalPort(47500);
//		tcpDiscoverySpi.setLocalPortRange(9);
//		igniteConfiguration.setDiscoverySpi(tcpDiscoverySpi);
//
//		TcpCommunicationSpi communicationSpi = new TcpCommunicationSpi();
//		communicationSpi.setLocalAddress(igniteHost);
//		communicationSpi.setLocalPort(48100);
//		communicationSpi.setSlowClientQueueLimit(1000);
//		igniteConfiguration.setCommunicationSpi(communicationSpi);
//
//		// Apply Ignite cache Configuration
//		igniteConfiguration.setCacheConfiguration(cacheConfiguration());
//
//		return igniteConfiguration;
//
//	}
//
//	/**
//	 * Create Cache configuration
//	 *
//	 * @return
//	 */
//	@Bean(name = "cacheConfiguration")
//	public CacheConfiguration[] cacheConfiguration() {
//		CacheConfiguration cacheConfiguration;
//		List<CacheConfiguration> cacheConfigurations = new ArrayList<>();
//
//		cacheConfiguration = createCacheConfiguration(CacheNode.DASHBOARD_SEARCH);
//		cacheConfigurations.add(cacheConfiguration);
//		cacheConfiguration.setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 180)));
//
//		cacheConfiguration = createCacheConfiguration(CacheNode.DOWNLOAD_LINK);
//		cacheConfigurations.add(cacheConfiguration);
//		cacheConfiguration.setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 180))); // Set Expiration Time to 3 Hours
//
//		cacheConfiguration = createCacheConfiguration(CacheNode.OTP_CODES);
//		cacheConfigurations.add(cacheConfiguration);
//		cacheConfiguration.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, 300))); // Set Expiration Time to 5 Mins
//
//		return cacheConfigurations.toArray(new CacheConfiguration[cacheConfigurations.size()]);
//	}
//
//	@NotNull
//	private CacheConfiguration createCacheConfiguration(String configurationName) {
//		CacheConfiguration cacheConfiguration = new CacheConfiguration();
//		cacheConfiguration.setAtomicityMode(CacheAtomicityMode.ATOMIC);
//		cacheConfiguration.setCacheMode(CacheMode.REPLICATED);
//		cacheConfiguration.setName(configurationName);
//		cacheConfiguration.setWriteThrough(false);
//		cacheConfiguration.setReadThrough(false);
//		cacheConfiguration.setWriteBehindEnabled(false);
//		cacheConfiguration.setBackups(1);
//		cacheConfiguration.setStatisticsEnabled(true);
//		return cacheConfiguration;
//	}

}
