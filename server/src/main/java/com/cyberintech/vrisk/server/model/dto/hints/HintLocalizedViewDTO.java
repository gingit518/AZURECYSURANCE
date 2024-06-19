package com.cyberintech.vrisk.server.model.dto.hints;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.domains.HintType;
import com.cyberintech.vrisk.server.model.jpa.entity.Hints;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;

/**
 * Hint Localized View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-04-05
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "code", "hintType"})
@EqualsAndHashCode(of = {"id", "code"}, callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HintLocalizedViewDTO extends DTOBase<Hints> {

	@Schema
	private Long id;

	@Schema
	private String code;

	@Schema
	private String name;

	@Schema
	private HintType hintType;

	@Schema
	private String title;

	@Schema
	private String body;

	@Schema
	private String footer;

	@Schema
	private String link;

	@Schema
	private Map<String, Object> properties;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public HintLocalizedViewDTO(Hints entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Hints entity) {
		// super.fromEntity(hints);

		id = entity.getId();
		name = entity.getName();
		code = entity.getCode();
		hintType = entity.getHintType();
		title = entity.getTitle();
		body = entity.getBody();
		footer = entity.getFooter();
		link = entity.getLink();
		properties = entity.getProperties();
	}
}
