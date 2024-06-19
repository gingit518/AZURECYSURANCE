package com.cyberintech.vrisk.server.model.dto.data_type_classification;

import com.cyberintech.vrisk.server.model.dto.datadomains.DataDomainsDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.DataTypeClassification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Data Type Classification Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-26
 */
@Setter
@Getter
@NoArgsConstructor
public class DataTypeClassificationEditDTO extends DataTypeClassificationViewDTO {

	@Schema
	private OrganizationRefDTO organization;

	@Schema
	private List<DataDomainsDTO> dataDomains;

	@Schema
	private List<DataFieldsDTO> dataFields;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public DataTypeClassificationEditDTO(DataTypeClassification entity) {
		super(entity);
	}

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public DataTypeClassificationEditDTO(DataTypeClassification entity, boolean isSuperAdmin) {
		super(entity);

		if (isSuperAdmin) {
			if (entity.getOrganization() != null) organization = new OrganizationRefDTO(entity.getOrganization());
		}
	}

	@Override
	public void fromEntity(DataTypeClassification entity) {
		super.fromEntity(entity);

		dataDomains = Optional.ofNullable(entity.getDataDomains()).orElse(new HashSet<>()).stream().map(DataDomainsDTO::new).collect(Collectors.toList());
		dataFields = Optional.ofNullable(entity.getDataFields()).orElse(new HashSet<>()).stream().map(DataFieldsDTO::new).collect(Collectors.toList());
	}
}
