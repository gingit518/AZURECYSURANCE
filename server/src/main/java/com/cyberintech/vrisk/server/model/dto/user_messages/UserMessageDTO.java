package com.cyberintech.vrisk.server.model.dto.user_messages;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.MessageStatus;
import com.cyberintech.vrisk.server.model.jpa.entity.UserMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * User Messages View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2023-01-11
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class UserMessageDTO extends DTOBase<UserMessages> {

	@Schema
	private Long id;

	@Schema
	private UserRefDTO messageFrom;

	@Schema
	private UserRefDTO messageTo;

	@Schema
	private Date createdAt;

	@Schema
	private Date updatedAt;

	@Schema
	private String subject;

	@Schema
	private String body;

	@Schema
	private MessageStatus messageStatus;

	@Schema
	private List<DocumentDTO> documents;


	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public UserMessageDTO(UserMessages entity) {
		super(entity);
	}

	@Override
	public void fromEntity(UserMessages entity) {
		id = entity.getId();
		messageFrom = new UserRefDTO(entity.getMessageFrom());
		messageTo = new UserRefDTO(entity.getMessageTo());
		createdAt = entity.getCreatedAt();
		updatedAt = entity.getUpdatedAt();
		subject = entity.getSubject();
		body = entity.getBody();
		messageStatus = entity.getStatus();
		documents = Optional.ofNullable(entity.getDocuments()).orElse(new HashSet<>()).stream()
			.map(DocumentDTO::new).collect(Collectors.toList());
	}


}
