package com.cyberintech.vrisk.server.repository.results;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemToolRiskReductionDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.SystemToolRiskReductions;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

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
@ToString(of = {"id", "system"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class SystemToolRiskReductionResult extends DTOBase {

	@Schema
	private Long id;

	@Schema
	private SystemRefDTO system;

	@Schema
	private SystemToolRiskReductionDTO systemToolRiskReduction;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public SystemToolRiskReductionResult(Systems system, SystemToolRiskReductions entity) {
		if (entity != null) {

			this.setId(entity.getId());
			if (entity.getSystem() != null) {
				this.system = new SystemRefDTO(entity.getSystem());
			}
			systemToolRiskReduction = new SystemToolRiskReductionDTO(entity);

		} else if (system != null) {
			this.system = new SystemRefDTO(system);
			systemToolRiskReduction = new SystemToolRiskReductionDTO();
			systemToolRiskReduction.setSystem(this.system);
		}
	}

}
