package com.cyberintech.vrisk.server.model.dto.datadomains;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.regulations.RegulationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.DataDomains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Data Domains Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-06-16
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class DataDomainsDTO extends DTOBase<DataDomains> {

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
	public DataDomainsDTO(DataDomains entity) {
		super(entity);
	}

	@Override
	public void fromEntity(DataDomains entity) {
		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
	}
}
