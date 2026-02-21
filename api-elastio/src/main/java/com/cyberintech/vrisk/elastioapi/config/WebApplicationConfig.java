package com.cyberintech.vrisk.elastioapi.config;

import com.cyberintech.vrisk.elastioapi.web.APIErrorAttributes;
import com.cyberintech.vrisk.server.model.converters.StringToDownloadTypeConverter;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Application Configuration. Override some BEANs.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-20
 */
@Configuration
public class WebApplicationConfig implements WebMvcConfigurer {

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new StringToDownloadTypeConverter());
	}

	/**
	 * Overriding Error Attributes
	 *
	 * @return
	 */
	@Bean
	public ErrorAttributes errorAttributes() {
		APIErrorAttributes result = new APIErrorAttributes();

		return result;
	}

}
