package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dto.dashboards.DashboardStateDTO;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Cache Storage Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-01
 */
@Service
@Slf4j
public class CacheService {

	@Autowired
	private CacheWrapperService cacheWrapperService;

	public DashboardStateDTO getSearchConfig(String key) {
		ObjectMapper jsonMapper = new ObjectMapper();
		jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		jsonMapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);

		DashboardStateDTO result = null;
		String jsonString = cacheWrapperService.getSearchConfigStringFromCache(key);
		try {
			if (jsonString != null) {
				result = jsonMapper.readValue(jsonString, DashboardStateDTO.class);
			}
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}

		return result;
	}

	public void saveSearchConfig(String key, DashboardStateDTO dashboardState) {
		ObjectMapper jsonMapper = new ObjectMapper();
		jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		jsonMapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);

		try {
			String jsonString = jsonMapper.writeValueAsString(dashboardState);
			cacheWrapperService.putSearchConfigStringToCache(key, jsonString);
		} catch (JsonProcessingException e) {
			log.warn(e.getMessage(), e);
		}
	}
}
