package com.cyberintech.vrisk.server.model.dto.organization;

import com.cyberintech.vrisk.server.model.dto.technology.TechnologyRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Organization View Entity Definition
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
public class VendorViewDTO extends OrganizationViewDTO {

	@Schema
	private Set<TechnologyRefDTO> technologies;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public VendorViewDTO(Organizations entity) {
		super(entity);
	}

	/**
	 * Converts from entity to DTO
	 *
	 * @param entity
	 */
	@Override
	public void fromEntity(Organizations entity) {
		super.fromEntity(entity);

		isTechnologyVendor = entity.getIsTechnologyVendor();
		isServiceVendor = entity.getIsServiceVendor();
		isSystemVendor = entity.getIsSystemVendor();

		technologies = entity.getTechnologies().stream().map(TechnologyRefDTO::new).collect(Collectors.toSet());
	}
}
