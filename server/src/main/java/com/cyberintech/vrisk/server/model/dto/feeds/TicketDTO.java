package com.cyberintech.vrisk.server.model.dto.feeds;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Ticket data object
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  2.0.14
 * @since    2022-11-28
 */
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketDTO extends DTOBase {

	@Schema
	private Long id;

	@Schema
	private String code;

	@Schema
	private String title;

}
