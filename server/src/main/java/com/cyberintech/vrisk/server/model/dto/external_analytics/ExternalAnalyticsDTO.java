package com.cyberintech.vrisk.server.model.dto.external_analytics;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.role.RoleListDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.ExternalAnalyticsType;
import com.cyberintech.vrisk.server.model.jpa.entity.ExternalAnalytics;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * External Analytics Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-10-13
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name", "externalAnalyticsType"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class ExternalAnalyticsDTO extends DTOBase<ExternalAnalytics> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private ExternalAnalyticsType externalAnalyticsType;

	@Schema
	private Long organizationId;

	@Schema
	private OrganizationRefDTO organization;

	@Schema
	private Set<ExternalAnalyticsParametersDTO> externalAnalyticsParameters;

	@Schema
	private Boolean isPublic;

	@Schema
	private Set<RoleListDTO> roles;

	@Schema
	private String logo;

	@Schema
	private DocumentDTO logoDocument;

	@Schema
	private Boolean removeLogo;


	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public ExternalAnalyticsDTO(ExternalAnalytics entity) {
		super(entity);
	}

	@Override
	public void fromEntity(ExternalAnalytics entity) {
//		super.fromEntity(entity);

		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
		organizationId = entity.getOrganizationId();
		externalAnalyticsType = entity.getExternalAnalyticsType();
		isPublic = entity.getIsPublic();
		if (entity.getOrganization() != null) organization = new OrganizationRefDTO(entity.getOrganization());

		externalAnalyticsParameters = Optional.ofNullable(entity.getExternalAnalyticsParameters()).orElse(new HashSet<>()).stream().map(ExternalAnalyticsParametersDTO::new).collect(Collectors.toSet());
		roles = Optional.ofNullable(entity.getRoles()).orElse(new HashSet<>()).stream().map(RoleListDTO::new).collect(Collectors.toSet());
		logo = entity.getLogo();
		if (entity.getLogoDocument() != null) setLogoDocument(new DocumentDTO(entity.getLogoDocument(), true));
	}
}
