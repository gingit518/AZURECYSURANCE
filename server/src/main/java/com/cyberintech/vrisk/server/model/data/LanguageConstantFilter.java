package com.cyberintech.vrisk.server.model.data;

import com.cyberintech.vrisk.server.model.jpa.domains.LanguageConstantScopeType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Implementation of Language Constant Filtering Object
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-04-21
 */
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"name"})
@EqualsAndHashCode(of = {"name"}, callSuper = false)
public class LanguageConstantFilter extends NameFilter {

	@Schema
	String value;

	@JsonIgnore
	String languageCode;

	@JsonIgnore
	LanguageConstantScopeType scope;

	/**
	 * Static constructor
	 *
	 * @param languageCode
	 * @param scope
	 * @return
	 */
	public static LanguageConstantFilter of(String languageCode, LanguageConstantScopeType scope) {
		LanguageConstantFilter filter = new LanguageConstantFilter();
		filter.setLanguageCode(languageCode);
		filter.setScope(scope);

		return filter;
	}

}
