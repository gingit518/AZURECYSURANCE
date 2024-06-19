package com.cyberintech.vrisk.server.model.dto.category_domain;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.risk_domains.RiskDomainViewDTO;
import com.cyberintech.vrisk.server.model.dto.risk_model_domains.RiskModelDomainViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.CategoryDomains;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskDomains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hibernate.Hibernate;

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
public class CategoryDomainViewDTO extends DTOWithMetaData<CategoryDomains> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private RiskModelDomainViewDTO domain;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public CategoryDomainViewDTO(CategoryDomains entity) {
		super(entity);
	}

	@Override
	public void fromEntity(CategoryDomains entity) {
//		super.fromEntity(entity);

		// Loading item metadata
		loadMetadata(entity);

		this.id = entity.getId();
		this.name = entity.getName();
		this.description = entity.getDescription();

		if (entity.getRiskModelDomain() != null) {
			// riskModelDomainId = entity.getRiskModelDomain().getId();
			domain = new RiskModelDomainViewDTO();
			domain.setId(entity.getRiskModelDomain().getId());
			RiskDomains riskDomain = (RiskDomains) Hibernate.unproxy(entity.getRiskModelDomain().getRiskDomain());
			domain.setRiskDomainView(new RiskDomainViewDTO(riskDomain));
		}
	}
}
