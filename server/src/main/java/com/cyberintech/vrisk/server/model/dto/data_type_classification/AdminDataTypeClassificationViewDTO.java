package com.cyberintech.vrisk.server.model.dto.data_type_classification;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.regulations.RegulationDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.DataTypeClassification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Data Type Classification View Entity Definition
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
public class AdminDataTypeClassificationViewDTO extends DataTypeClassificationViewDTO {

	@Schema
	private OrganizationRefDTO organization;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public AdminDataTypeClassificationViewDTO(DataTypeClassification entity) {
		super(entity);
	}

	@Override
	public void fromEntity(DataTypeClassification entity) {
		super.fromEntity(entity);
		setMetadataFromEntity(entity);

		setReadOnly(false);
		if (entity.getOrganization() != null) {
			organization = new OrganizationRefDTO(entity.getOrganization());
		}
	}
}
