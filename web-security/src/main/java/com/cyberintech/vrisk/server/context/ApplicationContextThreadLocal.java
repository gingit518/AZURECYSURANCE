package com.cyberintech.vrisk.server.context;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Holds Application context
 */
public abstract class ApplicationContextThreadLocal {

	private static final ThreadLocal<ApplicationContext> contextHolder = new ThreadLocal<ApplicationContext>();

	/**
	 * Set Application Context
	 *
	 * @param context
	 */
	public static void setContext(ApplicationContext context) {
		contextHolder.set(context);
	}

	/**
	 * Clear context
	 */
	public static void unsetContext() {
		contextHolder.remove();
	}

	/**
	 * Get Application Context
	 *
	 * @return
	 */
	public static ApplicationContext getContext() {
		return contextHolder.get();
	}

	@AllArgsConstructor
	@Getter
	@Setter
	public static class ApplicationContext {

		private Long organizationId;

		private String localeString;

		private Locale locale;

		private Map<String, Object> properties;

		/**
		 * Default constructor
		 */
		public ApplicationContext() {
			properties = new HashMap<>();
		}

		/**
		 * Get property for current context
		 *
		 * @param key
		 * @return
		 */
		public Object getProperty(String key) {
			return getProperty(key, null);
		}

		/**
		 * Get property for current context
		 *
		 * @param key
		 * @param defaultValue
		 * @return
		 */
		public Object getProperty(String key, Object defaultValue) {
			if (!properties.containsKey(key)) {
				properties.put(key, defaultValue);
			}

			return properties.get(key);
		}

		/**
		 * Set property for current context
		 *
		 * @param key
		 * @param valur
		 * @return
		 */
		public void setProperty(String key, Object valur) {
			properties.put(key, valur);
		}

	}
}
