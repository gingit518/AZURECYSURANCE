package com.cyberintech.vrisk.server.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * API info version
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  2.0.14
 * @since    2022-09-14
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"version", "releaseDate"})
@EqualsAndHashCode(of = {"version", "releaseDate"}, callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIVersionDTO {

	@Schema
	private String version;

	@Schema
	private String releaseDate;

	@Schema
	private String signature;

	/**
	 * Full arguments constructor
	 */
	public APIVersionDTO(String version, String releaseDate, String signature) {
		this.version = version;
		this.releaseDate = releaseDate;
		this.signature = signature;
	}

}
