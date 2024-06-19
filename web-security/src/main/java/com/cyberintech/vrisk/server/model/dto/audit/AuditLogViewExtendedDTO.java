package com.cyberintech.vrisk.server.model.dto.audit;

import com.cyberintech.vrisk.server.model.jpa.entity.AuditLog;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Audit Log View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-07-25
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class AuditLogViewExtendedDTO extends AuditLogViewDTO {

	private List<AuditLogRelatedOperationDTO> relatedOperations;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public AuditLogViewExtendedDTO(AuditLog entity) {
		super(entity);
	}

	/**
	 * Converts from entity to DTO
	 *
	 * @param entity
	 */
	@Override
	public void fromEntity(AuditLog entity) {
		super.fromEntity(entity);

		relatedOperations = Optional.ofNullable(entity.getAuditLogItemIds()).orElse(new HashSet<>()).stream().map(AuditLogRelatedOperationDTO::new).collect(Collectors.toList());
	}

}
