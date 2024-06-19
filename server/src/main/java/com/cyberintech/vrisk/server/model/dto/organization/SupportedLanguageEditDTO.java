package com.cyberintech.vrisk.server.model.dto.organization;

import com.cyberintech.vrisk.server.model.jpa.entity.SupportedLanguages;
import lombok.*;

/**
 * Supported Language Edit Entity Definition
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-03-16
 */
@Setter
@Getter
@NoArgsConstructor
public class SupportedLanguageEditDTO extends SupportedLanguageViewDTO {


	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public SupportedLanguageEditDTO(SupportedLanguages entity) {
		super(entity);
	}

	@Override
	public void fromEntity(SupportedLanguages entity) {
		super.fromEntity(entity);
	}
}
