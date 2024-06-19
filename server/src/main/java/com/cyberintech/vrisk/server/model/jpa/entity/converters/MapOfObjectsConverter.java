package com.cyberintech.vrisk.server.model.jpa.entity.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Map Value converter
 *
 * @author 	Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 	2022-04-04
 */
@Component
@Converter
public class MapOfObjectsConverter implements AttributeConverter<Map<String, Object>, String> {

	ObjectMapper mapper = new ObjectMapper();

	@SneakyThrows
	@Override
	public String convertToDatabaseColumn(Map<String, Object> attribute) {
		String dbData = null;

		if (attribute != null) {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();

			mapper.writeValue(outStream, attribute);
			dbData = new String(outStream.toByteArray());
		}

		return dbData;
	}

	@SneakyThrows
	@Override
	public Map<String, Object> convertToEntityAttribute(String dbData) {
		Map<String, Object> attribute = new HashMap<>();

		if (StringUtils.isNotEmpty(dbData)) {
			attribute = mapper.readValue(dbData, mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class));
		}

		return attribute;
	}

}
