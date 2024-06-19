package com.cyberintech.vrisk.server.model.dto.contract;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Contract;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;

/**
 * Contract View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2023-05-08
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class ContractDTO extends DTOWithMetaData<Contract> {

	@Schema
	private Long id;

	@Schema
	private OrganizationRefDTO organization;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private String number;

	@Schema
	private DocumentDTO document;

	@Schema
	private Date startDate;;

	@Schema
	private Date expiryDate;;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public ContractDTO(Contract entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Contract entity) {
		this.id = entity.getId();

		if (entity.getOrganization() != null) {
			organization = new OrganizationRefDTO(entity.getOrganization());
		}

		this.name = entity.getName();
		this.description = entity.getDescription();
		this.number = entity.getNumber();

		if (entity.getDocument() != null) {
			document = new DocumentDTO(entity.getDocument());
		}
		this.startDate = entity.getStartDate();
		this.expiryDate = entity.getExpiryDate();
	}


}
