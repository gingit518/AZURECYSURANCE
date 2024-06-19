package com.cyberintech.vrisk.server.model.dto.user;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.UsersMetadata;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = {"key"}, callSuper = false)
public class UserMetadataRefDTO extends DTOBase<UsersMetadata> {
	@Schema
	private Long id;
	@Schema
	private String key;
	@Schema
	private String value;

	public UserMetadataRefDTO(UsersMetadata usersMetadata) {
		super(usersMetadata);
	}

	@Override
	public void fromEntity(UsersMetadata usersMetadata) {
		this.id = usersMetadata.getId();
		this.key = usersMetadata.getKey();
		this.value = usersMetadata.getValue();
	}
}
