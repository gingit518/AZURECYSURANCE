package com.cyberintech.vrisk.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Language Constant Service.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-04-09
 */
@Service
public class LanguageConstantInitializerService {

	@Autowired
	private LanguageConstantService languageConstantService;

	@PostConstruct
	public void configure() {
		languageConstantService.loadLanguageConstants();
	}

}
