package com.cyberintech.vrisk.server.model.dto.technology_categories;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyCategories;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Technology Category View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-26
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class TechnologyCategoryViewDTO extends DTOWithMetaData<TechnologyCategories> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private boolean readOnly;

	@Schema
	private Set<TechnologyCategoryMetadataRefDTO> entityMetadata;


	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public TechnologyCategoryViewDTO(TechnologyCategories entity) {
		super(entity);
	}

	@Override
	public void fromEntity(TechnologyCategories entity) {
//		super.fromEntity(entity);

		// Loading item metadata
		loadMetadata(entity);

		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();

		readOnly = entity.getOrganizationId() == null;

		entityMetadata = Optional.ofNullable(entity.getMetadata())
			.stream()
			.flatMap(Set::stream).map(TechnologyCategoryMetadataRefDTO::new).collect(Collectors.toSet());
	}
}
