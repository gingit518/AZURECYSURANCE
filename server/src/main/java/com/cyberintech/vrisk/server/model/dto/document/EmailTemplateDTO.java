package com.cyberintech.vrisk.server.model.dto.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Email Template DTO Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2023-08-23
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateDTO {

	private String subject;

	private String htmlContent;

}
