package com.cyberintech.vrisk.server.model.dto.category_domain;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.risk_model_domains.RiskModelDomainViewDTO;
import com.cyberintech.vrisk.server.model.dto.risk_type.CategoryRiskTypeViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.CategoryDomains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;
import java.util.List;

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
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class CategoryDomainEditDTO extends DTOBase<CategoryDomains> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private RiskModelDomainViewDTO domain;

	@Schema
	private Long riskModelId;

	@Schema
	private List<CategoryRiskTypeViewDTO> riskTypes;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public CategoryDomainEditDTO(CategoryDomains entity) {
		super(entity);
	}

}
