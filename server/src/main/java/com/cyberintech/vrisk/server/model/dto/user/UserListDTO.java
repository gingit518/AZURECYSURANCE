package com.cyberintech.vrisk.server.model.dto.user;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.UserEmploymentType;
import com.cyberintech.vrisk.server.model.jpa.entity.Roles;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.*;
import java.util.stream.Collectors;

/**
 * User Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-10-27
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "email"})
@EqualsAndHashCode(of = {"id", "email"}, callSuper = false)
public class UserListDTO extends DTOWithMetaData<Users> {

	@Schema
	private Long id;

	@Schema
	private String fullName;

	@Schema
	private String firstName;

	@Schema
	private String lastName;

	@Schema
	private String email;

	@Schema
	private String title;

	private String corporatePhone;

	private String mobilePhone;

	@Schema
	private Boolean enabled;

	private Boolean expired;

	private Boolean credentialsExpired;

	private Boolean locked;

	private Date expirationDate;

	private Date credentialsExpirationDate;

	@Schema
	private BusinessUnitRefDTO businessUnit;

	private UserEmploymentType employmentType;

	private Set<UserMetadataViewDTO> entityMetadata;

	@Schema
	private DocumentDTO profilePicture;

	/**
	 * User role codes list
	 */
	@Schema(type = "array", name = "List of User role objects")
	private List<ItemViewDTO<Roles>> roles;

	/**
	 * Entity based constructor
	 *
	 * @param users
	 */
	public UserListDTO(Users users) {
		super(users);
	}

	@Override
	public void fromEntity(Users entity) {
//		super.fromEntity(entity);

		id = entity.getId();
		fullName = entity.getFullName();
		firstName = entity.getFirstName();
		lastName = entity.getLastName();
		email = entity.getEmail();
		title = entity.getTitle();
		corporatePhone = entity.getCorporatePhone();
		mobilePhone = entity.getMobilePhone();
		enabled = entity.getEnabled();
		expired = entity.getExpired();
		credentialsExpired = entity.getCredentialsExpired();
		locked = entity.getLocked();
		expirationDate = entity.getExpirationDate();
		credentialsExpirationDate = entity.getCredentialsExpirationDate();
		employmentType = entity.getEmploymentType();

		if (entity.getBusinessUnit() != null) {
			businessUnit = new BusinessUnitRefDTO(entity.getBusinessUnit());
		}

		if (entity.getProfilePicture() != null) {
			this.setProfilePicture(new DocumentDTO(entity.getProfilePicture(), true));
		}

		roles = Optional.ofNullable(entity.getRoles()).orElse(new HashSet<>()).stream()
			.map(roles1 -> new ItemViewDTO<Roles>(roles1)).collect(Collectors.toList());

		entityMetadata = Optional.ofNullable(entity.getMetadata())
			.stream()
			.flatMap(Set::stream).map(UserMetadataViewDTO::new).collect(Collectors.toSet());
	}
}
