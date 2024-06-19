package com.cyberintech.vrisk.server.model.dto.associate_models;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.AssociateModels;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Associate Model View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-26
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class AssociateModelViewDTO extends DTOWithMetaData<AssociateModels> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private UserRefDTO owner;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public AssociateModelViewDTO(AssociateModels entity) {
		super(entity);
	}

	@Override
	public void fromEntity(AssociateModels entity) {
		this.id = entity.getId();
		this.name = entity.getName();
		this.description = entity.getDescription();

		if (entity.getOwner() != null) {
			owner = new UserRefDTO(entity.getOwner());
		}
	}
}
