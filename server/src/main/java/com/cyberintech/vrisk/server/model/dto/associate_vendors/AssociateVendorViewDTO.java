package com.cyberintech.vrisk.server.model.dto.associate_vendors;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.organization.VendorViewDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.AssociateVendors;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Associate Vendow View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-15
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "vendor"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class AssociateVendorViewDTO extends DTOWithMetaData<AssociateVendors> {

	@Schema
	private Long id;

	@Schema
	private OrganizationRefDTO organization;

	@Schema
	private VendorViewDTO vendor;

	@Schema
	private List<SystemRefDTO> systems;


	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public AssociateVendorViewDTO(AssociateVendors entity) {
		super(entity);
	}

	@Override
	public void fromEntity(AssociateVendors entity) {
		this.id = entity.getId();

		if (entity.getOrganization() != null) {
			organization = new OrganizationRefDTO(entity.getOrganization());
		}

		if (entity.getVendor() != null) {
			vendor = new VendorViewDTO(entity.getVendor());
		}

		systems = Optional.ofNullable(entity.getSystems()).orElse(new HashSet<>()).stream().map(system -> new SystemRefDTO(system)).collect(Collectors.toList());
	}
}
