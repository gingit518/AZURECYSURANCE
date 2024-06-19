package com.cyberintech.vrisk.server.integration.bigid.batch.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public final class JsonHelper {
	private final ObjectMapper objectMapper;

	public <T> T fromJson(String s, Class<T> clazz) {
		if (StringUtils.isBlank(s)) {
			return null;
		}
		try {
			return objectMapper.readValue(s, clazz);
		} catch (Exception ex) {
			log.warn("Can not deserialize string into {}. Data = {}.", clazz.getName(), s);
			return null;
		}
	}

	public String toJson(Object o) {
		if (o == null) {
			return null;
		}
		try {
			return objectMapper.writeValueAsString(o);
		} catch (Exception ex) {
			log.warn("Can not serialize object into json. Object = {}.", o);
			return null;
		}
	}
}
