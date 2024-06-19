package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.config.CacheNode;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Cache Storage Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-01
 */
@Service
public class CacheWrapperService {

	@Autowired
	private ApplicationProperties applicationProperties;

	private static final String CACHE_KEY_PREFIX = "CACHE_KEY_";

	public static String getCacheKey(String key){
		return CACHE_KEY_PREFIX + key;
	}

	@Cacheable(cacheNames = CacheNode.DASHBOARD_SEARCH, key = "T(com.cyberintech.vrisk.server.service.CacheWrapperService).getCacheKey(#key)")
	public String getSearchConfigStringFromCache(String key) {
		return null;
	}

	@CachePut(cacheNames = CacheNode.DASHBOARD_SEARCH, key = "T(com.cyberintech.vrisk.server.service.CacheWrapperService).getCacheKey(#key)")
	public String putSearchConfigStringToCache(String key, String value) {
		return value;
	}

	@CacheEvict(cacheNames = CacheNode.DASHBOARD_SEARCH, key = "T(com.cyberintech.vrisk.server.service.CacheWrapperService).getCacheKey(#key)")
	public void removeSearchConfigStringFromCache(String relevant) {
	}

}
