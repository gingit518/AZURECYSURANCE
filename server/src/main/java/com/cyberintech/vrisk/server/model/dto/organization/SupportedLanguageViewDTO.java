package com.cyberintech.vrisk.server.model.dto.organization;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.domains.LanguageDirection;
import com.cyberintech.vrisk.server.model.jpa.entity.SupportedLanguages;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Supported Language View Entity Definition
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-03-16
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class SupportedLanguageViewDTO extends DTOBase<SupportedLanguages> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String code;

	@Schema
	private String charset;

	@Schema
	private String locale;

	@Schema
	private LanguageDirection direction;

	@Schema
	private Boolean isPublic;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public SupportedLanguageViewDTO(SupportedLanguages entity) {
		super(entity);
	}

	@Override
	public void fromEntity(SupportedLanguages entity) {
		id = entity.getId();
		name = entity.getName();
		code = entity.getCode();
		charset = entity.getCharset();
		locale = entity.getLocale();
		direction = entity.getDirection();
		isPublic = entity.getIsPublic();
	}
}
