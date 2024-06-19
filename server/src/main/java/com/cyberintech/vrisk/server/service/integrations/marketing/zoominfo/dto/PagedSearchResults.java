package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * ZoomInfo search results response
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-03-22
 */
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class PagedSearchResults<ENTITY> implements Serializable {

	@Schema
	private Long maxResults;

	@Schema
	private Long totalResults;

	@Schema
	private Long currentPage;

	@Schema
	private List<ENTITY> data;

	/**
	 * Default constructor
	 */
	public PagedSearchResults() {
	}

}
