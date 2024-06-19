package com.cyberintech.vrisk.server.model.data;

import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Implementation of System Filtering Logic
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-04-01
 */
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"name"})
@EqualsAndHashCode(of = {"name"}, callSuper = false)
public class SystemFilter extends NameFilter {

	@Schema
	private SystemStatus systemStatus;

	@Schema
	private DataAssetClassificationRefDTO assetClass;

	@Schema
	private DataTypeClassificationRefDTO dataType;

	@Schema
	private BusinessUnitRefDTO businessUnit;

	@Schema
	private UserRefDTO systemOwner;

	@Schema
	private Boolean isEtl;

}
