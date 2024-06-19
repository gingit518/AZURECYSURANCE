package com.cyberintech.vrisk.server.model.dto.technology;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.country.CountryViewDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyClassTypeDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologySubcategoryDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Technologies;
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
 * Technologies View Entity Definition
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
public class TechnologyViewDTO extends DTOWithMetaData<Technologies> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private String version;

	@Schema
	private Double riskReduction;

	@Schema
	private Double riskReductionPercent;

	@Schema
	private Double toolPrice;

	@Schema
	private String notes;

	@Schema
	private TechnologyCategoryRefDTO technologyCategory;

	@Schema
	private TechnologySubcategoryDTO technologySubcategory;

	@Schema
	private TechnologyClassTypeDTO technologyClassType;

	@Schema
	private OrganizationRefDTO vendor;

	@Schema
	private CountryViewDTO country;

	@Schema
	private UserRefDTO businessOwner;

	@Schema
	private UserRefDTO itOwner;

	@Schema
	private Set<TechnologyMetadataRefDTO> entityMetadata;

	@Schema
	private UserRefDTO infosecFocalPerson;

	@Schema
	private Long organizationOwnerId;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public TechnologyViewDTO(Technologies entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Technologies entity) {
//		super.fromEntity(entity);

		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
		version = entity.getVersion();
		notes = entity.getNotes();
		riskReduction = entity.getRiskReduction();
		riskReductionPercent = entity.getRiskReductionPercent();
		toolPrice = entity.getToolPrice();

		if (entity.getTechnologyCategory() != null) {
			technologyCategory = new TechnologyCategoryRefDTO(entity.getTechnologyCategory());
		}

		if (entity.getTechnologySubcategory() != null) {
			technologySubcategory = new TechnologySubcategoryDTO(entity.getTechnologySubcategory());
		}

		if (entity.getTechnologyClassType() != null) {
			technologyClassType = new TechnologyClassTypeDTO(entity.getTechnologyClassType());
		}

		if (entity.getVendor() != null) {
			vendor = new OrganizationRefDTO(entity.getVendor());
		}

		if (entity.getCountry() != null) {
			country = new CountryViewDTO(entity.getCountry());
		}

		if (entity.getItOwner() != null) {
			itOwner = new UserRefDTO(entity.getItOwner());
		}

		if (entity.getBusinessOwner() != null) {
			businessOwner = new UserRefDTO(entity.getBusinessOwner());
		}

		entityMetadata = Optional.ofNullable(entity.getMetadata())
			.stream()
			.flatMap(Set::stream).map(TechnologyMetadataRefDTO::new).collect(Collectors.toSet());

		if (entity.getInfosecFocalPerson() != null) {
			infosecFocalPerson = new UserRefDTO(entity.getInfosecFocalPerson());
		}

		organizationOwnerId = entity.getOrganizationOwnerId();
	}
}
