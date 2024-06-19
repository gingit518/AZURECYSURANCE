package com.cyberintech.vrisk.server.model.dto.field_classifier;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.FieldClassifiers;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Systems View Entity Definition
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2018-12-27
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class FieldClassifierRefDTO extends DTOBase<FieldClassifiers> {

	@Schema
	private Long id;

	@Schema
	private String name;

	public FieldClassifierRefDTO(FieldClassifiers entity) {
		super(entity);
	}

	@Override
	public void fromEntity(FieldClassifiers entity) {
		id = entity.getId();
		name = entity.getName();
	}
}
