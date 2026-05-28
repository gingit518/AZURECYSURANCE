package com.cyberintech.vrisk.server.model.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Implementation of Name Filtering Logic
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2026-02-22
 */
@NoArgsConstructor
@Setter
@Getter
public class ElastioOrganizationFilter extends NameFilter {
	@JsonIgnore
	private List<Long> packagePlanIds;
}
