package com.cyberintech.vrisk.server.repository.results;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.organization.VendorViewDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.AssociateVendors;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Associate Vendor View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-05-15
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "vendor"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class AssociateVendorResult extends DTOBase {

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
	public AssociateVendorResult(Organizations vendor, AssociateVendors entity) {
		if (entity != null) {

			this.setId(entity.getId());
			if (entity.getVendor() != null) {
				this.vendor = new VendorViewDTO(entity.getVendor());
			}
			systems = Optional.ofNullable(entity.getSystems()).orElse(new HashSet<>()).stream().map(system -> new SystemRefDTO(system)).collect(Collectors.toList());

		} else if (vendor != null) {
			this.vendor = new VendorViewDTO(vendor);
			systems = new ArrayList<>();
		}
	}

}
