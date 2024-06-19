package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
@Slf4j
public class ComplianceRuleQueryUtil {

	public static final String RAW_PATTERN = "field IN \\((.+)\\)";
	private static final String CLASSIFIER_PREFIX = "classifier.";
	private static final Pattern FIELD_IN_PATTERN = Pattern.compile(RAW_PATTERN, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

	public static Set<String> parseListOfFieldClassifiers(String query) {
		log.info("Parsing field in field classifiers query: {}", query);
		Matcher matcher = FIELD_IN_PATTERN.matcher(query);
		if (!matcher.matches()) {
			log.warn("Query does not match '{}' pattern. Skipped.", RAW_PATTERN);
			return Collections.emptySet();
		}

		String group = StringUtils.trim(matcher.group(1));
		log.info("Field Classifier stringified list: {}", group);
		if (StringUtils.isBlank(group)) {
			return Collections.emptySet();
		}

		Set<String> fieldClassifiers = new LinkedHashSet<>();
		for (String classifier : group.split(",")) {
			String fieldClassifier = unquote(classifier);
			if (StringUtils.startsWithIgnoreCase(fieldClassifier, CLASSIFIER_PREFIX)) {
				fieldClassifier = StringUtils.removeStart(fieldClassifier, CLASSIFIER_PREFIX);
			}
			fieldClassifiers.add(fieldClassifier);
		}
		log.info("Parsed field classifiers: {}", fieldClassifiers);
		return fieldClassifiers;
	}

	private static String unquote(String classifier) {
		return StringUtils.removeEnd(StringUtils.removeStart(StringUtils.trimToEmpty(classifier), "\""), "\"");
	}

}
