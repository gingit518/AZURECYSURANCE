package com.cyberintech.vrisk.server.model.dto.risk_domains;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskDomains;
import lombok.*;

/**
 * Risk Domain View Entity Definition
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
public class RiskDomainViewDTO extends DTOBase<RiskDomains> {

	private Long id;
	private String name;
	private String description;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public RiskDomainViewDTO(RiskDomains entity) {
		super(entity);
	}
}
