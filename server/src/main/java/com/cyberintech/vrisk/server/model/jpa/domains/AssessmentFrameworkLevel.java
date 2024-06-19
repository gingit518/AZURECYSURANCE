package com.cyberintech.vrisk.server.model.jpa.domains;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Assessment framework level
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-06-06
 */
@JsonFormat(shape = JsonFormat.Shape.NUMBER)
public enum AssessmentFrameworkLevel {
	NONE, IG1, IG2, IG3
}
