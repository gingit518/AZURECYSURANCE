package com.cyberintech.vrisk.server.util;

import java.io.Serializable;
import java.util.Locale;

/**
 * Client message interface to apply proper Resource Bundles for client resources.
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 */
public interface ClientMessage extends Serializable {

	/**
	 * Gets simple message for current Client locate
	 *
	 * @param	messageKey
	 * @return
	 */
	String getMessage(String messageKey);

	/**
	 * Gets simple message for current Client locate
	 *
	 * @param	messageKey
	 * @return
	 */
	String getMessage(String messageKey, String defaultTranslation);

	/**
	 * Gets message for current Client locale
	 *
	 * @param	messageKey
	 * @param	replacements
	 * @return
	 */
	String getMessage(String messageKey, Object[] replacements);

	/**
	 * Gets message for some specified locate
	 *
	 * @param	messageKey
	 * @param	replacements
	 * @param	locale
	 * @return
	 */
	String getMessage(String messageKey, Object[] replacements, Locale locale);

	/**
	 * Gets message for some specified locate
	 *
	 * @param	messageKey
	 * @param	replacements
	 * @param	locale
	 * @return
	 */
	String getMessage(String messageKey, Object[] replacements, Locale locale, String defaultTranslation);

}
