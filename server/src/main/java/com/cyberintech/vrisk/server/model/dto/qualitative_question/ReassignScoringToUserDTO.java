package com.cyberintech.vrisk.server.model.dto.qualitative_question;

import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Reassign User for one of the Scoring types
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-05-14
 */
@Setter
@Getter
@ToString(of = {"user", "item", "itemType"})
@EqualsAndHashCode(of = {"user", "item", "itemType"}, callSuper = false)
public class ReassignScoringToUserDTO {

	@Schema
	private UserRefDTO user;

	@Schema
	private ItemViewDTO item;

	@Schema
	private VendorType itemType;

	@Schema
	private Boolean reassignOwner;

	/**
	 * Default constructor
	 */
	public ReassignScoringToUserDTO() {
	}

}
