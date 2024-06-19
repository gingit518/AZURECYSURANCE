package com.cyberintech.vrisk.server.model.dto.organization;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.PackagePlans;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Package Plans Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-07-22
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class PackagePlansDTO extends DTOBase<PackagePlans> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public PackagePlansDTO(PackagePlans entity) {
		super(entity);
	}

	@Override
	public void fromEntity(PackagePlans entity) {
		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
	}
}
