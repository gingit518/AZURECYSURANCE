package com.cyberintech.vrisk.server.model.dto.external_analytics;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.ExternalAnalyticsParameters;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * External Analytics Parameter Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-10-13
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"name"}, callSuper = false)
public class ExternalAnalyticsParametersDTO extends DTOBase<ExternalAnalyticsParameters> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String value;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public ExternalAnalyticsParametersDTO(ExternalAnalyticsParameters entity) {
		super(entity);
	}

	@Override
	public void fromEntity(ExternalAnalyticsParameters entity) {
//		super.fromEntity(entity);

		id = entity.getId();
		name = entity.getName();
		value = entity.getValue();
	}

	@Override
	public ExternalAnalyticsParameters toEntity(ExternalAnalyticsParameters origEntity) {
		ExternalAnalyticsParameters result = origEntity != null ? origEntity : new ExternalAnalyticsParameters();
		result.setName(this.getName());
		result.setValue(this.getValue());

		return result;
	}
}
