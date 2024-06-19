package com.cyberintech.vrisk.idp;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.awt.image.BufferedImage;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, ErrorMvcAutoConfiguration.class})
@ComponentScan(basePackages = {"com.cyberintech"})
@EnableWebMvc
@EnableEncryptableProperties
@EnableJms
public class IdpServerApplication {

	public static void main(String[] args) {
		// Initialize Jasypt Encryption Details
		if (StringUtils.isEmpty(System.getProperty("jasypt.encryptor.password"))) {
			if (StringUtils.isNotEmpty(System.getenv("RISKQ_SECRET"))) {
				System.setProperty("jasypt.encryptor.password", System.getenv("RISKQ_SECRET"));
			} else {
				System.setProperty("jasypt.encryptor.password", "R@skQ873Gh78Fh");
			}
		}

		SpringApplication.run(IdpServerApplication.class, args);
	}

}
