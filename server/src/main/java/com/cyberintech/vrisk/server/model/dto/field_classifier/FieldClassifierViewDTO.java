package com.cyberintech.vrisk.server.model.dto.field_classifier;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.FieldClassifiers;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class FieldClassifierViewDTO extends DTOBase<FieldClassifiers> {

	@Schema
	private Long id;
	@Schema
	private String name;
	@Schema
	private String description;

	@Schema
	private Set<FieldClassifierMetadataRefDTO> entityMetadata;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public FieldClassifierViewDTO(FieldClassifiers entity) {
		super(entity);
	}

	@Override
	public void fromEntity(FieldClassifiers entity) {
		this.id = entity.getId();
		this.name = entity.getName();
		this.description = entity.getDescription();
		entityMetadata = Optional.ofNullable(entity.getMetadata())
			.stream()
			.flatMap(Set::stream).map(FieldClassifierMetadataRefDTO::new).collect(Collectors.toSet());
	}
}
