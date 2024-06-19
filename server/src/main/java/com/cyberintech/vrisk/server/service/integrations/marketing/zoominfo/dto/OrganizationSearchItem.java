package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentViewDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.ControlMaturityViewDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.SecurityRequirementDTO;
import com.cyberintech.vrisk.server.model.dto.control_category.ControlCategoryRefDTO;
import com.cyberintech.vrisk.server.model.dto.control_function.ControlFunctionRefDTO;
import com.cyberintech.vrisk.server.model.dto.control_subcategory.ControlSubcategoryRefDTO;
import com.cyberintech.vrisk.server.model.dto.tasks.TaskViewDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.AssessmentFindingLink;
import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentFindings;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ZoomInfo Organization search item
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-02-23
 */
@Setter
@Getter
public class OrganizationSearchItem extends SearchItem {

	/**
	 * Default constructor
	 */
	public OrganizationSearchItem() {
		super();
	}

}
