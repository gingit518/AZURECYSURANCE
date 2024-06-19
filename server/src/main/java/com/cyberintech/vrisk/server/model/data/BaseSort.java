package com.cyberintech.vrisk.server.model.data;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.util.Map;

/**
 * Base Sort Parameter
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-04
 */
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"field", "order"})
@EqualsAndHashCode(of = {"field", "order"})
public class BaseSort implements Serializable {

	private static final long serialVersionUID = 240772768088139782L;

	@Schema(example = "id")
	private String field;

	@Schema(example = "ASC", allowableValues = "ASC,DESC")
	private SortOrder order;

	@Schema(hidden = true)
	@Hidden
	private Map<String, String> sortMapping;


	/**
	 * Elementary Sort Ordering types
	 */
	public enum SortOrder {
		ASC, DESC
	}

	/**
	 * Check is sort set
	 *
	 * @return
	 */
	@Hidden
	public boolean isInitialized() {
		boolean result = false;

		if (StringUtils.isNotEmpty(field)) {
			result = true;
		}

		return result;
	}

	/**
	 * Get Spring Sort
	 *
	 * @return
	 */
	@Hidden
	public Sort toSort() {
		Sort result = null;

		if (StringUtils.isNotEmpty(field)) {

			String sortFieldName = field;
			if (sortMapping != null && StringUtils.isNotEmpty(field) && sortMapping.containsKey(field)) {
				sortFieldName = sortMapping.get(field);
			}

			if (SortOrder.ASC.equals(order)) {
				result = Sort.by(Sort.Order.asc(sortFieldName));
			} else {
				result = Sort.by(Sort.Order.desc(sortFieldName));
			}
		}

		return result;
	}

	/**
	 * Get ORDER String
	 *
	 * @return
	 */
	@Hidden
	public String toOrderString() {
		return toOrderString(null);
	}

	/**
	 * Get ORDER String
	 *
	 * @return
	 */
	@Hidden
	@Deprecated
	/*
	  FIXME Fix all the code to use toOrderStringSafe method, which prevents HQL injection.
	  Hibernate supports UNION from 6.0 version which could be used for HQL injection in this case
	  https://docs.jboss.org/hibernate/orm/6.0/userguide/html_single/Hibernate_User_Guide.html#hql-set-operators
	*/
	public String toOrderString(Map<String, String> sortMapping) {
		String result = "";

		String sortFieldName = field;
		if (sortMapping != null && StringUtils.isNotEmpty(field) && sortMapping.containsKey(field)) {
			sortFieldName = sortMapping.get(field);
		}

		if (StringUtils.isNotEmpty(sortFieldName)) {
			String fieldName = sortFieldName.replaceAll("[^a-zA-Z\\_\\-\\.]", "");
			if (SortOrder.ASC.equals(order)) {
				result = " ORDER BY " + fieldName + " ASC";
			} else {
				result = " ORDER BY " + fieldName + " DESC";
			}
		}

		return result;
	}

	/**
	 * Get ORDER String.
	 * If {@link field} doesn't present in sort mapping, the data would not be sorted.
	 *
	 * @return
	 */
	@Hidden
	public String toOrderStringSafe(Map<String, String> sortMapping) {
		String result = "";

		if (sortMapping != null && StringUtils.isNoneEmpty(field)) {
			String sortFieldName = sortMapping.get(field);

			if (StringUtils.isNotEmpty(sortFieldName)) {
				if (SortOrder.ASC.equals(order)) {
					result = " ORDER BY " + sortFieldName + " ASC";
				} else {
					result = " ORDER BY " + sortFieldName + " DESC";
				}
			}
		}

		return result;
	}

	/**
	 * Static constructor
	 *
	 * @param field
	 * @param order
	 * @return
	 */
	public static BaseSort of(String field, SortOrder order) {
		BaseSort result = new BaseSort();
		result.setField(field);
		result.setOrder(order);

		return result;
	}

}
