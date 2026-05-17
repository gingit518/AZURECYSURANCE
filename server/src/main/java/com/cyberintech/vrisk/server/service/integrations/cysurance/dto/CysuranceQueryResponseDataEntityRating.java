package com.cyberintech.vrisk.server.service.integrations.cysurance.dto;

import lombok.Data;

import java.util.List;

@Data
public class CysuranceQueryResponseDataEntityRating {
	private String factor_code;
	private String category_code;
	private String factor_name;
	private String value_type;
	private Object value;
	private String measured_at;
	private String reported_by;
	private String received_at;
	private Double confidence;
}
