package com.cyberintech.vrisk.server.model.dto.external_analytics;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.ExternalAnalyticsParameterType;
import com.cyberintech.vrisk.server.model.jpa.domains.ExternalAnalyticsType;
import com.cyberintech.vrisk.server.model.jpa.entity.ExternalAnalytics;
import com.cyberintech.vrisk.server.model.jpa.entity.ExternalAnalyticsParameters;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Optional;

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
public class ExternalAnalyticsViewDTO extends DTOBase<ExternalAnalytics> {

	@Schema
	private Long id;

	@Schema
	private Long dashboardId;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private ExternalAnalyticsType externalAnalyticsType;

	@Schema
	private DocumentDTO logoDocument;


	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public ExternalAnalyticsViewDTO(ExternalAnalytics entity) {
		super(entity);
	}

	@Override
	public void fromEntity(ExternalAnalytics entity) {
//		super.fromEntity(entity);

		id = entity.getId();
		dashboardId = 1000000000 + entity.getId();
		name = entity.getName();
		description = entity.getDescription();
		externalAnalyticsType = entity.getExternalAnalyticsType();
		if (entity.getLogoDocument() != null) setLogoDocument(new DocumentDTO(entity.getLogoDocument(), true));

		if (ExternalAnalyticsType.DASHBOARD.equals(externalAnalyticsType)) {
			Optional<ExternalAnalyticsParameters> dashboardIdOpt = entity.getExternalAnalyticsParameters().stream().filter(externalAnalyticsParameter -> ExternalAnalyticsParameterType.DASHBOARD_REPORT_ID.name().equalsIgnoreCase(externalAnalyticsParameter.getName())).findFirst();
			if (dashboardIdOpt.isPresent()) {
				try {
					dashboardId = Long.parseLong(dashboardIdOpt.get().getValue().trim());
				} catch (NumberFormatException exception) {
					;
				}
			}
		}
	}
}
