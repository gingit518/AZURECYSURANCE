package com.cyberintech.vrisk.server.model.data;

import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;

/**
 * Base Filtered Request
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-04
 */
@Setter
@Getter
@ToString(of = {"page", "size", "total", "items"})
@EqualsAndHashCode(of = {"page", "size"})
public class FilteredResponse<T extends BaseFilter, DTO extends DTOBase> implements Serializable {

	private static final long serialVersionUID = -7511860082290939443L;

	@Schema(example = "0")
	private int page;

	@Schema(description = "Size of the page")
	private int size;

	@Schema(description = "Total number of items")
	private int total;

	private int pages;

	private BaseSort sort;

	private T filter;

	private List<DTO> items;

	/**
	 * Initialize default response parameters
	 */
	public FilteredResponse() {
		page = 0;
		size = 10;

		sort = new BaseSort();
	}

	@Schema(description = "Total number of pages")
	public int getPages(){
		pages = 0;

		// Canculate total number of pages
		if (total > 0 && size > 0) {
			pages = Math.round(total / size);

			if (total % size > 0) pages++;
		}

		return pages;
	}

	/**
	 * Initialize response parameters
	 */
	public FilteredResponse(FilteredRequest<T> filteredRequest) {
		page = filteredRequest.getPage();
		size = filteredRequest.getSize();
		sort = filteredRequest.getSort();
		filter = filteredRequest.getFilter();
	}

	/**
	 * Initialize response parameters
	 */
	public FilteredResponse(FilteredRequest<T> filteredRequest, PagedResult<DTO> pagedResult) {
		this(filteredRequest);

		if (pagedResult != null) {
			items = pagedResult.getItems();
			total = (pagedResult.getCount() != null) ? pagedResult.getCount().intValue() : 0;
		}
	}

	/**
	 * Initialize response parameters
	 */
	public FilteredResponse(Pageable pageable) {
		page = pageable.getPageNumber();
		size = pageable.getPageSize();
	}

	/**
	 * Static constructor
	 *
	 * @param filteredRequest
	 * @return
	 */
	public static FilteredResponse of(FilteredRequest filteredRequest) {
		FilteredResponse filteredResponse = new FilteredResponse(filteredRequest);

		return filteredResponse;
	}

	/**
	 * Static constructor
	 *
	 * @param pageable
	 * @return
	 */
	public static FilteredResponse of(Pageable pageable) {
		FilteredResponse filteredResponse = new FilteredResponse(pageable);

		return filteredResponse;
	}

	/**
	 * Easy items setter
	 *
	 * @param items
	 * @return
	 */
	public FilteredResponse items(List<DTO> items) {
		this.setItems(items);

		return this;
	}

}
