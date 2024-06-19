package com.cyberintech.vrisk.server.model.dto.category_domain;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.risk_domains.RiskDomainViewDTO;
import com.cyberintech.vrisk.server.model.dto.risk_model_domains.RiskModelDomainViewDTO;
import com.cyberintech.vrisk.server.model.dto.risk_type.CategoryRiskTypeViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.CategoryDomains;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskDomains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Risk Model Domain View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-08
 */
@Setter
@Getter
@NoArgsConstructor
public class CategoryDomainDetailsDTO extends CategoryDomainViewDTO {

	@Schema
	private List<CategoryRiskTypeViewDTO> riskTypes;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public CategoryDomainDetailsDTO(CategoryDomains entity) {
		super(entity);
	}

	@Override
	public void fromEntity(CategoryDomains entity) {
		super.fromEntity(entity);

		riskTypes = Optional.ofNullable(entity.getRiskTypes()).orElse(new HashSet<>()).stream().map(CategoryRiskTypeViewDTO::new).collect(Collectors.toList());
	}
}
