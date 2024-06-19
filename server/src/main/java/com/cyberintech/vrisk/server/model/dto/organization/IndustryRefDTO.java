package com.cyberintech.vrisk.server.model.dto.organization;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.entity.Industries;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import lombok.*;

/**
 * Industry Reference Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-07-08
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class IndustryRefDTO extends DTOBase<Industries> {

	private Long id;
	// private IndustryRefDTO parent;
	private Long naicsCode;
	private Long naicsCodeUpper;
	private String name;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public IndustryRefDTO(Industries entity) {
		super(entity);
	}
}
