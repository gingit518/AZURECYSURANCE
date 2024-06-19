package com.cyberintech.vrisk.server.model.dto.policy;


import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.PolicyReviewType;
import com.cyberintech.vrisk.server.model.jpa.entity.PolicyReviews;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;

/**
 * Policy Review View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.0
 * @since    2023-09-24
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "notes"})
@EqualsAndHashCode(of = {"id", "notes"}, callSuper = false)
public class PolicyReviewViewDTO extends DTOBase<PolicyReviews> {

	@Schema
	private Long id;

	@Schema
	private Long policyId;

	@Schema
	private String notes;

	@Schema
	private PolicyReviewType reviewType;

	@Schema
	private UserRefDTO reviewer;

	@Schema
	private Date reviewDate;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public PolicyReviewViewDTO(PolicyReviews entity) {
		super(entity);
	}

	@Override
	public void fromEntity(PolicyReviews entity) {
	//	super.fromEntity(entity);
		id = entity.getId();
		policyId = entity.getPolicyId();
		notes = entity.getNotes();
		reviewType = entity.getReviewType();
		reviewDate = entity.getReviewDate();
		if (entity.getReviewer() != null) reviewer = new UserRefDTO(entity.getReviewer());
	}
}
