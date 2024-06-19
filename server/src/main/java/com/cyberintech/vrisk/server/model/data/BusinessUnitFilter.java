package com.cyberintech.vrisk.server.model.data;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Implementation of Business Unit Filtering Logic
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-02-06
 */
@NoArgsConstructor
@Setter
@Getter
public class BusinessUnitFilter extends NameFilter {

	private Long organizationId;

}
