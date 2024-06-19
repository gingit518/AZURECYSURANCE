package com.cyberintech.vrisk.server.model.dto.associate_vendors;

import com.cyberintech.vrisk.server.model.dto.associate_models.AssociateModelViewDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.QualMetricsViewDTO;
import com.cyberintech.vrisk.server.model.dto.quant_metrics.QuantMetricsViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.AssociateModels;
import com.cyberintech.vrisk.server.model.jpa.entity.AssociateVendors;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Associate Vendor Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-15
 */
@Setter
@Getter
@NoArgsConstructor
public class AssociateVendorEditDTO extends AssociateVendorViewDTO {

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public AssociateVendorEditDTO(AssociateVendors entity) {
		super(entity);
	}

	@Override
	public void fromEntity(AssociateVendors entity) {
		super.fromEntity(entity);
	}
}
