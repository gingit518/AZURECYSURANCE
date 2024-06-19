package com.cyberintech.vrisk.server.model.dto.dashboards.elements;

import com.cyberintech.vrisk.server.model.dto.dashboards.DashboardDataItemDrilldownDTO;
import com.cyberintech.vrisk.server.model.dto.dashboards.DashboardLinkDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * Dashboard Grid Item Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-12-19
 */
@Setter
@Getter
@ToString(of = {"value", "color"})
public class BaseDashboardElementDTO<E> {

	private E value;
	private String color;
	private String backgroundColor;
	private Long colSpan;
	private Long rowSpan;
	private String textAlign;
	private Map<String, String> params;
	private DashboardDataItemDrilldownDTO drilldown;
	private DashboardLinkDTO link;

	public BaseDashboardElementDTO() {
		params = new HashMap<>();
	}

	public BaseDashboardElementDTO(E value) {
		this();
		this.value = value;
	}

	public BaseDashboardElementDTO(E value, DashboardDataItemDrilldownDTO drilldown) {
		this(value);
		this.drilldown = drilldown;
	}

	/**
	 * Clone Item
	 *
	 * @return
	 */
	public BaseDashboardElementDTO clone() {
		BaseDashboardElementDTO result = new BaseDashboardElementDTO();
		result.setValue(getValue());
		result.setColor(getColor());
		result.setBackgroundColor(getBackgroundColor());
		result.setColSpan(getColSpan());
		result.setRowSpan(getRowSpan());
		result.setTextAlign(getTextAlign());

		return result;
	}

	/**
	 * Apply Colspan
	 *
	 * @param colspan
	 * @return
	 */
	public BaseDashboardElementDTO applyColspan(Long colspan) {
		setColSpan(colspan);

		return this;
	}

	/**
	 * Apply Rowspan
	 *
	 * @param rowspan
	 * @return
	 */
	public BaseDashboardElementDTO applyRowspan(Long rowspan) {
		setRowSpan(rowspan);

		return this;
	}

	/**
	 * Apply Text Align
	 *
	 * @param textAlign
	 * @return
	 */
	public BaseDashboardElementDTO applyTextAlign(String textAlign) {
		setTextAlign(textAlign);

		return this;
	}

	/**
	 * Apply Background Color
	 *
	 * @param backgroundColor
	 * @return
	 */
	public BaseDashboardElementDTO applyBackgroundColor(String backgroundColor) {
		setBackgroundColor(backgroundColor);

		return this;
	}

	/**
	 * Apply Color
	 *
	 * @param color
	 * @return
	 */
	public BaseDashboardElementDTO applyColor(String color) {
		setColor(color);

		return this;
	}

	/**
	 * Apply Drilldown items
	 *
	 * @param drilldown
	 * @return
	 */
	public BaseDashboardElementDTO applyDrilldown(DashboardDataItemDrilldownDTO drilldown) {
		setDrilldown(drilldown);

		return this;
	}

	/**
	 * Apply Link to the item
	 *
	 * @param link
	 * @return
	 */
	public BaseDashboardElementDTO applyLink(DashboardLinkDTO link) {
		setLink(link);

		return this;
	}

}
