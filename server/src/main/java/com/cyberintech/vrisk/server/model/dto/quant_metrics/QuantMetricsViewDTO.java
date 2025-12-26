package com.cyberintech.vrisk.server.model.dto.quant_metrics;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationViewDTO;
import com.cyberintech.vrisk.server.model.dto.organization.IndustryDTO;
import com.cyberintech.vrisk.server.model.dto.organization.IndustryRefDTO;
import com.cyberintech.vrisk.server.model.dto.regulations.RegulationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.QuantMetricLevel;
import com.cyberintech.vrisk.server.model.jpa.entity.Industries;
import com.cyberintech.vrisk.server.model.jpa.entity.QuantMetrics;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Quantification Metrics View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-19
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class QuantMetricsViewDTO extends DTOWithMetaData<QuantMetrics> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private Long ordinal;

	@Schema
	private QuantsRefDTO quant;

	@Schema
	private List<MetricFormulaItemViewDTO> metricFormulaItems;

	@Schema
	private List<DataTypeClassificationViewDTO> dataTypeClassifications;

	@Schema
	private QuantMetricLevel quantMetricLevel;

	@Schema
	private List<RegulationRefDTO> regulations;

	@Schema
	private String formula;

	@Schema
	private List<IndustryRefDTO> industries;

	@Schema
	private String measurementUnit;

	@Schema
	private String unitUIPosition;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public QuantMetricsViewDTO(QuantMetrics entity) {
		super(entity);
	}

	@Override
	public void fromEntity(QuantMetrics entity) {
//		super.fromEntity(entity);

		this.id = entity.getId();
		this.name = entity.getName();
		this.description = entity.getDescription();
		this.ordinal = entity.getOrdinal();
		this.quantMetricLevel = entity.getQuantMetricLevel();
		this.measurementUnit = entity.getMeasurementUnit();
		this.unitUIPosition = entity.getUnitUIPosition();

		if (entity.getQuant() != null) {
			quant = new QuantsRefDTO(entity.getQuant());
		}

		metricFormulaItems = Optional.ofNullable(entity.getMetricFormulaItems()).orElse(new HashSet<>()).stream().map(MetricFormulaItemViewDTO::new).collect(Collectors.toList());
		dataTypeClassifications = Optional.ofNullable(entity.getDataTypeClassifications()).orElse(new HashSet<>()).stream().map(DataTypeClassificationViewDTO::new).collect(Collectors.toList());
		regulations = Optional.ofNullable(entity.getRegulations()).orElse(new HashSet<>()).stream().map(RegulationRefDTO::new).collect(Collectors.toList());
		industries = Optional.ofNullable(entity.getIndustries()).orElse(new HashSet<>()).stream().map(IndustryRefDTO::new).collect(Collectors.toList());
	}
}
