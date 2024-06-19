package com.cyberintech.vrisk.server.model.dto.data_type_classification;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.field_classifier.FieldClassifierRefDTO;
import com.cyberintech.vrisk.server.model.dto.regulations.RegulationRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.DataTypeClassification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
public class DataTypeClassificationViewDTO extends DTOWithMetaData<DataTypeClassification> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private boolean readOnly;

	@Schema
	private List<RegulationRefDTO> regulations;

	@Schema
	private Set<DataTypeClassificationMetadataRefDTO> entityMetadata;

	@Schema
	private List<FieldClassifierRefDTO> fieldClassifiers;

	@Schema
	private UserRefDTO owner;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public DataTypeClassificationViewDTO(DataTypeClassification entity) {
		super(entity);
	}

	@Override
	public void fromEntity(DataTypeClassification entity) {
		setMetadataFromEntity(entity);

		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();

		readOnly = entity.getOrganizationId() == null;

		regulations = Optional.ofNullable(entity.getRegulations()).orElse(new HashSet<>()).stream().map(RegulationRefDTO::new).collect(Collectors.toList());

		entityMetadata = Optional.ofNullable(entity.getMetadata())
			.stream()
			.flatMap(Set::stream).map(DataTypeClassificationMetadataRefDTO::new).collect(Collectors.toSet());

		fieldClassifiers = Optional.ofNullable(entity.getFieldClassifiers())
			.stream()
			.flatMap(Set::stream)
			.map(FieldClassifierRefDTO::new).collect(Collectors.toList());

		owner = Optional.ofNullable(entity.getOwner()).map(UserRefDTO::new).orElse(null);
	}
}
