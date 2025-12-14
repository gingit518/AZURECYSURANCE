package com.cyberintech.vrisk.server.model.dto.dashboards;

import com.cyberintech.vrisk.server.service.dashboards.DashboardDataEvaluator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dashboard Grid Item Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-02-13
 */
@Setter
@Getter
@ToString(of = {"value", "color"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DashboardDataItemDTO {

	@Schema
	private Double valueDouble;
	private String value;
	private String valueRef;
	private String color;
	private String backgroundColor;
	private Long colSpan;
	private Long rowSpan;
	private String symbol;
	private String textAlign;
	private String type;
	private Map<String, String> params;

	@Schema
	private boolean isHeader = false;

	@Schema
	private boolean sortable = false;

	private DashboardDataItemDrilldownDTO drilldown;

	private DashboardLinkDTO link;

	private Integer digits;

	public DashboardDataItemDTO() {
		params = new HashMap<>();
	}

	public DashboardDataItemDTO(Double value) {
		this();
		this.value = value != null ? value.toString() : null;
		this.valueDouble = value;
	}

	public DashboardDataItemDTO(String value) {
		this();
		this.value = value;
	}

	public DashboardDataItemDTO(Double value, DashboardDataItemDrilldownDTO drilldown) {
		this(value);
		this.drilldown = drilldown;
	}

	public DashboardDataItemDTO(String value, DashboardDataItemDrilldownDTO drilldown) {
		this(value);
		this.drilldown = drilldown;
	}

	public DashboardDataItemDTO(Double value, String textAlign, String symbol) {
		this(value);
		this.textAlign = textAlign;
		this.symbol = symbol;
	}

	public DashboardDataItemDTO(String value, String textAlign, String symbol) {
		this();
		this.value = value;
		this.textAlign = textAlign;
		this.symbol = symbol;
	}

	public DashboardDataItemDTO(Double value, String textAlign, String symbol, DashboardDataItemDrilldownDTO drilldown) {
		this(value, textAlign, symbol);

		this.drilldown = drilldown;
	}

	public DashboardDataItemDTO(String value, String textAlign, String symbol, DashboardDataItemDrilldownDTO drilldown) {
		this(value, textAlign, symbol);

		this.drilldown = drilldown;
	}

	public DashboardDataItemDTO(Double value, boolean isHeader) {
		this(value);

		this.isHeader = isHeader;
	}

	public DashboardDataItemDTO(String value, boolean isHeader) {
		this(value);

		this.isHeader = isHeader;
	}

	/**
	 * Clone Item
	 *
	 * @return
	 */
	public DashboardDataItemDTO clone() {
		DashboardDataItemDTO result = new DashboardDataItemDTO();
		result.setValue(getValue());
		result.setColor(getColor());
		result.setBackgroundColor(getBackgroundColor());
		result.setColSpan(getColSpan());
		result.setRowSpan(getRowSpan());
		result.setSymbol(getSymbol());
		result.setHeader(isHeader());
		result.setTextAlign(getTextAlign());

		return result;
	}

	/**
	 * Create Dashboar Items from List
	 *
	 * @param items
	 * @param textAlign
	 * @param symbol
	 * @return
	 */
	public static List<DashboardDataItemDTO> createFromList(List<String> items, String textAlign, String symbol) {
		List<DashboardDataItemDTO> itemsList;
		if (items != null) {
			itemsList = items.stream().map(value -> {
				DashboardDataItemDTO result = new DashboardDataItemDTO(value);
				result.setTextAlign(textAlign);
				result.setSymbol(symbol);

				return result;
			}).collect(Collectors.toList());
		} else {
			itemsList = new ArrayList<>();
		}

		return itemsList;
	}

	/**
	 * Create Dashboard Items from List
	 *
	 * @param items
	 * @param textAlign
	 * @param symbol
	 * @return
	 */
	/*
	public static List<DashboardDataItemDTO> createFromList(List<Double> items, String textAlign, String symbol) {
		List<DashboardDataItemDTO> itemsList;
		if (items != null) {
			itemsList = items.stream().map(value -> {
				DashboardDataItemDTO result = new DashboardDataItemDTO(value);
				result.setTextAlign(textAlign);
				result.setSymbol(symbol);

				return result;
			}).collect(Collectors.toList());
		} else {
			itemsList = new ArrayList<>();
		}

		return itemsList;
	}
	*/

	/**
	 * Get Value Double
	 *
	 * @return
	 */
	public Double getValueDouble() {
		Double result = null;
		if (valueDouble != null) {
			result = valueDouble;
		} else {
			try {
				if (value != null) {
					result = Double.valueOf(value);
				}
			} catch (NumberFormatException e) {
			}
		}

		return result;
	}

	/**
	 * Apply Colspan
	 *
	 * @param colspan
	 * @return
	 */
	public DashboardDataItemDTO applyColspan(Long colspan) {
		setColSpan(colspan);

		return this;
	}

	/**
	 * Apply value
	 *
	 * @param value
	 * @return
	 */
	public DashboardDataItemDTO applyValue(Double value) {
		this.setValue(value != null ? value.toString() : null);
		this.setValueDouble(value);

		return this;
	}

	/**
	 * Apply Rowspan
	 *
	 * @param rowspan
	 * @return
	 */
	public DashboardDataItemDTO applyRowspan(Long rowspan) {
		setRowSpan(rowspan);

		return this;
	}

	/**
	 * Apply Text Align
	 *
	 * @param textAlign
	 * @return
	 */
	public DashboardDataItemDTO applyTextAlign(String textAlign) {
		setTextAlign(textAlign);

		return this;
	}

	/**
	 * Apply Background Color
	 *
	 * @param backgroundColor
	 * @return
	 */
	public DashboardDataItemDTO applyBackgroundColor(String backgroundColor) {
		setBackgroundColor(backgroundColor);

		return this;
	}

	/**
	 * Apply Color
	 *
	 * @param color
	 * @return
	 */
	public DashboardDataItemDTO applyColor(String color) {
		setColor(color);

		return this;
	}

	/**
	 * Apply Type
	 *
	 * @param type
	 * @return
	 */
	public DashboardDataItemDTO applyType(String type) {
		setType(type);

		return this;
	}

	/**
	 * Apply as Header
	 *
	 * @param isHeader
	 * @return
	 */
	public DashboardDataItemDTO applyHeader(boolean isHeader) {
		setHeader(isHeader);

		return this;
	}

	/**
	 * Apply custom parameter
	 *
	 * @param paramName
	 * @param paramValue
	 * @return
	 */
	public DashboardDataItemDTO applyParam(String paramName, String paramValue) {

		if (params == null) params = new HashMap<>();
		if (paramValue != null) {
			params.put(paramName, paramValue);
		} else if (params.containsKey(paramName)) {
			params.remove(paramName);
		}

		return this;
	}

	/**
	 * Apply as Sortable
	 *
	 * @param sortable
	 * @return
	 */
	public DashboardDataItemDTO applySortable(boolean sortable) {
		setSortable(sortable);

		return this;
	}

	/**
	 * Apply Drilldown items
	 *
	 * @param drilldown
	 * @return
	 */
	public DashboardDataItemDTO applyDrilldown(DashboardDataItemDrilldownDTO drilldown) {
		setDrilldown(drilldown);

		return this;
	}

	/**
	 * Apply Link to the item
	 *
	 * @param link
	 * @return
	 */
	public DashboardDataItemDTO applyLink(DashboardLinkDTO link) {
		setLink(link);

		return this;
	}

	/**
	 * Round double to shorten value and apply to String
	 *
	 * @return
	 */
	public DashboardDataItemDTO round() {
		return round(2);
	}

	/**
	 * Round double to shorten value and return as String
	 *
	 * @param level
	 * @return
	 */
	public DashboardDataItemDTO round(int level) {
		digits = level;
		String format = "%,.0f";
		if (level > 0) format = MessageFormat.format("%,.{0}f", level);

		setValue(String.format(format, getValueDouble()));

		return this;
	}

	/**
	 * Evaluate Data defined as valueRef in the DashboardDataItemDTO and all another corresponding fields
	 */
	@JsonIgnore
	public void evaluate(DashboardDataEvaluator dataEvaluator) {
		// Evaluate Data defined as valueRef in the DashboardDataItemDTO and all another corresponding fields
		if (dataEvaluator != null && StringUtils.isNoneEmpty(valueRef)) {
			String evaluatedValue = dataEvaluator.evaluate(valueRef);
			if (evaluatedValue != null) {
				value = evaluatedValue;
				if (digits != null) {
					round(digits);
				}
			}
		}
	}

}
