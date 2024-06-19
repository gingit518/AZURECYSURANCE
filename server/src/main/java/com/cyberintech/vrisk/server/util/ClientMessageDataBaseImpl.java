package com.cyberintech.vrisk.server.util;

import com.cyberintech.vrisk.server.context.ApplicationContextThreadLocal;
import com.cyberintech.vrisk.server.service.LanguageConstantService;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/**
 * Implementation for ClientMessage with Data Base as Source
 *
 * @author Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 */
@Component
public class ClientMessageDataBaseImpl implements ClientMessage {

	@Override
	public String getMessage(String messageKey) {
		return getMessage(messageKey, null, null, null);
	}

	@Override
	public String getMessage(String messageKey, String defaultTranslation) {
		return getMessage(messageKey, null, null, defaultTranslation);
	}

	@Override
	public String getMessage(String messageKey, Object[] replacements) {
		return getMessage(messageKey, replacements, null);
	}

	@Override
	// replacements and locale are ignored in current implementation
	public String getMessage(String messageKey, Object[] replacements, Locale locale) {
		return getMessage(messageKey, replacements, locale, null);
	}

	@Override
	public String getMessage(String messageKey, Object[] replacements, Locale locale, String defaultTranslation) {
		String localeString = ApplicationContextThreadLocal.getContext().getLocaleString();
		String result = null;
		if (LanguageConstantService.serverLanguageConstants.containsKey(localeString) && LanguageConstantService.serverLanguageConstants.get(localeString).containsKey(messageKey)) {
			result = LanguageConstantService.serverLanguageConstants.get(localeString).get(messageKey);
		} else {
			result = LanguageConstantService.serverDefaultLanguageConstants.get(messageKey);
		}
		if (result == null) {
			result = defaultTranslation != null ? defaultTranslation : messageKey;
		}

		return result;
	}

}
