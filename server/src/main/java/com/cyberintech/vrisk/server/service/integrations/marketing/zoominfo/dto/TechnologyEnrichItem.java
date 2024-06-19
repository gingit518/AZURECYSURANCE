package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ZoomInfo search item
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-03-22
 */
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TechnologyEnrichItem extends DTOBase<Object> {

	@Schema
	@JsonProperty("tag")
	private String uid;

	@Schema
	@JsonProperty("categoryParent")
	private String technologyCategory;

	@Schema
	@JsonProperty("category")
	private String technology;

	@Schema
	@JsonProperty("vendor")
	private String vendor;

	@Schema
	@JsonProperty("product")
	private String system;

	@Schema
	@JsonProperty("description")
	private String description;

	@Schema
	@JsonProperty("attribute")
	private String version;

	@Schema
	@JsonProperty("website")
	private String website;

	@Schema
	@JsonProperty("logo")
	private String logo;

	@Schema
	@JsonProperty("domain")
	private String domain;

	@Schema
	@JsonProperty("createdTime")
	private String createdTime;

	@Schema
	@JsonProperty("modifiedTime")
	private String modifiedTime;

	/**
	 * Default constructor
	 */
	public TechnologyEnrichItem() {
	}

}
