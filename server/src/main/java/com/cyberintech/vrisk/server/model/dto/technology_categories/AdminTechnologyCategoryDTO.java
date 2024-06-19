package com.cyberintech.vrisk.server.model.dto.technology_categories;

import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyCategories;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Technology Category View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-04-14
 */
@Setter
@Getter
@NoArgsConstructor
public class AdminTechnologyCategoryDTO extends TechnologyCategoryEditDTO {

	@Schema
	private OrganizationRefDTO organization;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public AdminTechnologyCategoryDTO(TechnologyCategories entity) {
		super(entity);
	}

	@Override
	public void fromEntity(TechnologyCategories entity) {
		super.fromEntity(entity);
		setMetadataFromEntity(entity);

		setReadOnly(false);
		if (entity.getOrganization() != null) {
			organization = new OrganizationRefDTO(entity.getOrganization());
		}
	}
}
