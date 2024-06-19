package com.cyberintech.vrisk.server.model.data;

import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;

/**
 * Implementation of Technology Asset Filtering Logic
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2024-01-22
 */
@NoArgsConstructor
@Setter
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class TechnologyAssetFilter extends NameFilter {

	@Schema
	private SystemStatus systemStatus;

	@Schema
	private String ipAddress;

	@Schema
	private TechnologyCategoryRefDTO technologyCategory;

	@Schema
	private TechnologyRefDTO technology;

	@Schema
	private OrganizationRefDTO manufacturer;

	@Schema
	private String assetOwner;

	@Schema
	private Date endOfLife;

	@Schema
	private String location;

}
