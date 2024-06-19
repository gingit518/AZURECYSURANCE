package com.cyberintech.vrisk.server.model.dto.business_unit;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.BusinessUnits;
import com.cyberintech.vrisk.server.model.jpa.entity.BusinessUnits;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Business Unit View Entity Definition
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
public class BusinessUnitViewDTO extends DTOWithMetaData<BusinessUnits> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private UserRefDTO owner;

	@Schema
	private UserRefDTO infosecFocalPerson;

	@Schema
	private BusinessUnitRefDTO parent;

	@Schema
	private OrganizationRefDTO subsidiaryOrganization;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public BusinessUnitViewDTO(BusinessUnits entity) {
		super(entity);
	}

	@Override
	public void fromEntity(BusinessUnits entity) {
//		super.fromEntity(entity);

		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();


		if (entity.getOwner() != null) {
			owner = new UserRefDTO(entity.getOwner());
		}

		if (entity.getInfosecFocalPerson() != null) {
			infosecFocalPerson = new UserRefDTO(entity.getInfosecFocalPerson());
		}

		if (entity.getParent() != null) {
			parent = new BusinessUnitRefDTO(entity.getParent());
		}

		if (entity.getSubsidiaryOrganization() != null) {
			subsidiaryOrganization = new OrganizationRefDTO(entity.getSubsidiaryOrganization());
		}
	}
}
