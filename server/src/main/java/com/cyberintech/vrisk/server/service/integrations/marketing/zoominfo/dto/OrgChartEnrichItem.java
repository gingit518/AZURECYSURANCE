package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * ZoomInfo Ogr Chart enrich item
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-03-27
 */
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrgChartEnrichItem extends DTOBase<Object> {

	/*
	{
		"id": 2423531975,
		"firstName": "Karnav",
		"middleName": "",
		"lastName": "Patel",
		"lastUpdatedDate": "",
		"title": "Senior Management Information Systems Financial Analyst",
		"hasEmail": true,
		"hasDirectPhone": true,
		"department": "Information Technology",
		"jobFunction": "Infrastructure",
		"orgChartTier": 7,
		"orgChartSubTier": 5,
		"company": {
			"id": 41340207,
			"name": "NIP Group"
		},
		"person": {
			"contactAccuracyScore": 95.0
		}
	}
	*/

	@Schema
	@JsonProperty("id")
	private Long id;

	@Schema
	private String firstName;

	@Schema
	private String middleName;

	@Schema
	private String lastName;

	@Schema
	private String title;

	@Schema
	private Boolean hasEmail;

	@Schema
	private Boolean hasDirectPhone;

	@Schema
	private String department;

	@Schema
	private String jobFunction;

	@Schema
	private Long orgChartTier;

	@Schema
	private Long orgChartSubTier;

	@Schema
	private SearchItem company;

	@Schema
	private Map<String, Object> person;

	/**
	 * Default constructor
	 */
	public OrgChartEnrichItem() {
	}

}
