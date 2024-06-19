package com.cyberintech.vrisk.server.model.dto.technology_categories;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyCategoriesMetadata;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = {"key"}, callSuper = false)
public class TechnologyCategoryMetadataRefDTO extends DTOBase<TechnologyCategoriesMetadata> {
	@Schema
	private Long id;
	@Schema
	private String key;
	@Schema
	private String value;

	public TechnologyCategoryMetadataRefDTO(TechnologyCategoriesMetadata technologyCategoriesMetadata) {
		super(technologyCategoriesMetadata);
	}

	@Override
	public void fromEntity(TechnologyCategoriesMetadata technologyCategoriesMetadata) {
		this.id = technologyCategoriesMetadata.getId();
		this.key = technologyCategoriesMetadata.getKey();
		this.value = technologyCategoriesMetadata.getValue();
	}
}
