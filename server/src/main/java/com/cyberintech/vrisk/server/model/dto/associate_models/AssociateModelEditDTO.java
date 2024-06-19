package com.cyberintech.vrisk.server.model.dto.associate_models;

import com.cyberintech.vrisk.server.model.dto.qual_metrics.QualMetricsViewDTO;
import com.cyberintech.vrisk.server.model.dto.quant_metrics.QuantMetricsViewDTO;
import com.cyberintech.vrisk.server.model.dto.quant_metrics.QuantsViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.AssociateModels;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Associate Model Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-26
 */
@Setter
@Getter
@NoArgsConstructor
public class AssociateModelEditDTO extends AssociateModelViewDTO {

	@Schema
	private Long riskModelId;

	@Schema
	private List<QualMetricsViewDTO> qualMetrics;

	@Schema
	private List<QuantsViewDTO> quantMetrics;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public AssociateModelEditDTO(AssociateModels entity) {
		super(entity);
	}

	@Override
	public void fromEntity(AssociateModels entity) {
		super.fromEntity(entity);

		this.riskModelId = entity.getRiskModelId();

		qualMetrics = Optional.ofNullable(entity.getQualMetrics()).orElse(new HashSet<>()).stream().map(QualMetricsViewDTO::new).collect(Collectors.toList());
		quantMetrics = Optional.ofNullable(entity.getQuantMetrics()).orElse(new HashSet<>()).stream().map(QuantsViewDTO::new).collect(Collectors.toList());
	}
}
