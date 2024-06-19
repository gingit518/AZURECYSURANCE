package com.cyberintech.vrisk.server.model.converters;

import com.cyberintech.vrisk.server.model.jpa.domains.DownloadType;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converter for DownloadType values
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-04-13
 */
@NoArgsConstructor
@Slf4j
public class StringToDownloadTypeConverter implements Converter<String, DownloadType> {
	@Override
	public DownloadType convert(String valueString) {
		DownloadType result = null;
		try {
			result = DownloadType.ofString(valueString);
		} catch (Exception exception) {
			log.warn("Failed to convert Download type from the value: " + valueString);
		}
		return result;
	}
}
