package com.cyberintech.vrisk.server.model.dto.business_unit;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.BusinessUnits;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Optional;

/**
 * Business Unit View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class BusinessUnitRefDTO extends DTOBase<BusinessUnits> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private BusinessUnitRefDTO parent;

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private long level = 0l;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public BusinessUnitRefDTO(BusinessUnits entity) {
		super(entity);
	}

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public BusinessUnitRefDTO(BusinessUnits entity, Long level) {
		// Current level of recursion
		this.level = Optional.ofNullable(level).orElse(0l);

		// Init from Entity
		if (entity != null) {
			fromEntity(entity);
		}
	}

	@Override
	public void fromEntity(BusinessUnits entity) {
		// super.fromEntity(entity);
		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();

		// Limit level of recursion to 8
		if (entity.getParent() != null && this.level < 8) {
			parent = new BusinessUnitRefDTO(entity.getParent(), this.level + 1);
		}
	}
}
