package com.cyberintech.vrisk.server.model.data;

import com.cyberintech.vrisk.server.model.dto.audit.ItemTypeDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import lombok.*;

import java.util.Date;
import java.util.List;

/**
 * Implementation of Audit Logs Filtering Logic
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-04-01
 */
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"itemTypes", "users"})
@EqualsAndHashCode()
public class AuditLogFilter extends NameFilter {

	private List<ItemTypeDTO> itemTypes;

	private Long itemId;

	private Long organizationId;

	private List<UserRefDTO> users;

	private Date dateFrom;

	private Date dateTo;

}
