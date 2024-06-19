package com.cyberintech.vrisk.server.model.dto.quant_metrics;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationViewDTO;
import com.cyberintech.vrisk.server.model.dto.organization.IndustryRefDTO;
import com.cyberintech.vrisk.server.model.dto.regulations.RegulationRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.DeploymentType;
import com.cyberintech.vrisk.server.model.jpa.domains.QuantMetricLevel;
import com.cyberintech.vrisk.server.model.jpa.entity.QuantMetrics;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Quantification Metrics Edit Entity Definition
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuantMetricsEditDTO extends DTOBase<QuantMetrics> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private Long riskModelId;

	@Schema
	private Long ordinal;

	@Schema
	private QuantsRefDTO quant;

//	@Schema
//	private List<MetricVariableViewDTO> metricVariables;

	@Schema
	private List<MetricFormulaItemViewDTO> metricFormulaItems;

	@Schema
	private List<DataTypeClassificationViewDTO> dataTypeClassifications;

	@Schema
	private List<TechnologyCategoryRefDTO> technologyCategories;

	@Schema
	private List<TechnologyRefDTO> technologies;

	@Schema
	private List<RegulationRefDTO> regulations;

	@Schema
	private QuantMetricLevel quantMetricLevel;

	@Schema
	private List<IndustryRefDTO> industries;

	@Schema
	private DeploymentType deploymentType;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public QuantMetricsEditDTO(QuantMetrics entity) {
		super(entity);
	}

	@Override
	public void fromEntity(QuantMetrics entity) {
		this.id = entity.getId();
		this.name = entity.getName();
		this.description = entity.getDescription();
		this.riskModelId = entity.getRiskModelId();
		this.ordinal = entity.getOrdinal();
		this.quantMetricLevel = entity.getQuantMetricLevel();
		this.deploymentType = entity.getDeploymentType();

		if (entity.getQuant() != null) {
			quant = new QuantsRefDTO(entity.getQuant());
		}

//		metricVariables = Optional.ofNullable(entity.getMetricVariables()).orElse(new HashSet<>()).stream().map(MetricVariableViewDTO::new).collect(Collectors.toList());
		metricFormulaItems = Optional.ofNullable(entity.getMetricFormulaItems()).orElse(new HashSet<>()).stream().map(MetricFormulaItemViewDTO::new).sorted(Comparator.comparingLong(MetricFormulaItemViewDTO::getOrdinal)).collect(Collectors.toList());
		dataTypeClassifications = Optional.ofNullable(entity.getDataTypeClassifications()).orElse(new HashSet<>()).stream().map(DataTypeClassificationViewDTO::new).collect(Collectors.toList());
		technologyCategories = Optional.ofNullable(entity.getTechnologyCategories()).orElse(new HashSet<>()).stream().map(TechnologyCategoryRefDTO::new).collect(Collectors.toList());
		technologies = Optional.ofNullable(entity.getTechnologies()).orElse(new HashSet<>()).stream().map(TechnologyRefDTO::new).collect(Collectors.toList());
		regulations = Optional.ofNullable(entity.getRegulations()).orElse(new HashSet<>()).stream().map(RegulationRefDTO::new).collect(Collectors.toList());
		industries = Optional.ofNullable(entity.getIndustries()).orElse(new HashSet<>()).stream().map(IndustryRefDTO::new).collect(Collectors.toList());
	}

}
