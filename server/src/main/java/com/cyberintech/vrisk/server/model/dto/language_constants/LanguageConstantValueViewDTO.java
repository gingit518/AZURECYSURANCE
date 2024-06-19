package com.cyberintech.vrisk.server.model.dto.language_constants;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.organization.SupportedLanguageViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.LanguageConstantValues;
import com.cyberintech.vrisk.server.model.jpa.entity.LanguageConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Language Constant Value View Entity Definition
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-04-17
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "value"})
@EqualsAndHashCode(of = {"id", "value"}, callSuper = false)
public class LanguageConstantValueViewDTO extends DTOBase<LanguageConstantValues> {

	@Schema
	private Long id;

	@Schema
	private String value;

	@Schema
	private SupportedLanguageViewDTO language;

	@Schema
	private LanguageConstants languageConstant;

	@Schema
	private String defaultValue;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public LanguageConstantValueViewDTO(LanguageConstantValues entity) {
		super(entity);
		defaultValue = "";
	}

	/**
	 * LanguageConstant based constructor
	 * @param entity
	 */
	public LanguageConstantValueViewDTO(LanguageConstants languageConstant, LanguageConstantValues entity, String defaultValue) {
		if (entity == null) {
			this.id = null;
			this.value = "";
			this.languageConstant = languageConstant;
			this.language = null;
			this.defaultValue = defaultValue;
		} else {
			this.id = entity.getId();
			this.value = entity.getValue();
			this.language = new SupportedLanguageViewDTO(entity.getLanguage());
			this.languageConstant = languageConstant;
			this.defaultValue = defaultValue;
		}
	}

	@Override
	public void fromEntity(LanguageConstantValues entity) {
		id = entity.getId();
		value = entity.getValue();
		language = new SupportedLanguageViewDTO(entity.getLanguage());
		languageConstant = entity.getLanguageConstant();
		defaultValue = "";
	}
}
