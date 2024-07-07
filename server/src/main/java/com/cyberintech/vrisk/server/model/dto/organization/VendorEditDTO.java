package com.cyberintech.vrisk.server.model.dto.organization;

import com.cyberintech.vrisk.server.model.dto.contract.ContractDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Contract;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.repository.jpa.ContractRepository;
import com.cyberintech.vrisk.server.util.BeanUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
public class VendorEditDTO extends OrganizationEditDTO {

	@Schema
	private List<SystemRefDTO> systems;

	@Schema
	private Set<TechnologyRefDTO> technologies;

	@Schema
	private Boolean isTechnologyVendor;

	@Schema
	private Boolean isSystemVendor;

	@Schema
	private Boolean isServiceVendor;

	@Schema
	private ContractDTO contract;




	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public VendorEditDTO(Organizations entity) {
		super(entity);
		this.systems = Collections.emptyList();
		ContractRepository contractRepository = BeanUtil.getBean(ContractRepository.class);
		Optional<Contract> vendorOpt = contractRepository.findByVendorId(this.getId());
		this.contract = vendorOpt.map(ContractDTO::new).orElseGet(ContractDTO::new);
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
		isSystemVendor = entity.getIsSystemVendor();
		isServiceVendor = entity.getIsServiceVendor();

		this.systems = Collections.emptyList();
		technologies = entity.getTechnologies().stream().map(TechnologyRefDTO::of).collect(Collectors.toSet());
	}

}
