package com.cyberintech.vrisk.server.model.data.queue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class RiskQQueueMessage implements Serializable {

	@Serial
	private static final long serialVersionUID = 5803189075141328950L;

	private String type;
	private Map<String, Object> body;
	private Long userId;
	private Long organizationId;
	private Date createDate;

}
