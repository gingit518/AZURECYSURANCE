package com.cyberintech.vrisk.server.model.data;

import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Implementation of Process Filtering Logic
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-04-01
 */
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"businessUnitOwns", "businessUnitUses", "system"}, callSuper = true)
@EqualsAndHashCode(of = {"name"}, callSuper = false)
public class ProcessFilter extends NameFilter {

	@Schema
	private BusinessUnitRefDTO businessUnitOwns;

	@Schema
	private BusinessUnitRefDTO businessUnitUses;

	@Schema
	private SystemRefDTO system;

	@Schema
	private DataTypeClassificationRefDTO dataType;

}
