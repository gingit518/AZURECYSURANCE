package com.cyberintech.vrisk.server.model.dto.policy;

import com.cyberintech.vrisk.server.model.jpa.entity.Policies;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Policy Review Entity Definition
 *
 * @author	 Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since 	 2023-09-24
 */
@Setter
@Getter
@NoArgsConstructor
public class PolicyReviewDTO extends PolicyEditDTO {

	@Schema
	private List<PolicyReviewViewDTO> reviews;

	@Schema
	private String reviewText;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public PolicyReviewDTO(Policies entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Policies entity) {
		super.fromEntity(entity);
		// reviews = Optional.ofNullable(entity.get()).orElse(new ArrayList<>()).stream().map(PolicyStatementViewDTO::new).collect(Collectors.toList());
	}
}
