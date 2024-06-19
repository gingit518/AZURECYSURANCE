package com.cyberintech.vrisk.server.model.dto.hints;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;

/**
 * Hint import Data Object
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-04-05
 */
@Setter
@Getter
@ToString(of = {"type", "name", "link"})
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class HintImportDTO {

	@Schema
	private String type;

	@Schema
	private String name;

	@Schema
	private String link;

	/**
	 * Default constructor
	 */
	public HintImportDTO() {
	}

	/**
	 * Build code from name
	 *
	 * @return
	 */
	@Transient
	public String getCode() {
		String result = null;

		if (name != null) {
			result = name.replaceAll("\\$", "_");
		}

		return result;
	}

}
