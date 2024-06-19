package com.cyberintech.vrisk.server.model.dto.language;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Language View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class LanguageViewDTO extends DTOBase<Language> {

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

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public LanguageViewDTO(Language entity) {
		super(entity);
	}

}
