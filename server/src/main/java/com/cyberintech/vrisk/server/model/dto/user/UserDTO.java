package com.cyberintech.vrisk.server.model.dto.user;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Roles;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.service.UserService;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
@ToString(of = {"id", "email"})
@EqualsAndHashCode(of = {"id", "email"}, callSuper = false)
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO extends DTOWithMetaData<Users> {

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

	// private String password;

	@Schema
	private String corporatePhone;

	@Schema
	private String mobilePhone;

	@Schema
	private Boolean expired;

	@Schema
	private Boolean enabled;

	/**
	 * User role codes list
	 */
	@Schema(type = "array", name = "List of User role objects")
	private List<ItemViewDTO<Roles>> roles;

	/**
	 * User role codes list
	 */
	@Schema(type = "array", name = "List of User role codes")
	private List<String> roleNames;

	/**
	 * User role permissions list
	 */
	@Schema(type = "array", name = "Permission codes")
	private Set<String> permissions;

	@Schema
	private Boolean credentialsExpired;

	@Schema
	private Boolean locked;

	@Schema
	private Date expirationDate;

	@Schema
	private Date credentialsExpirationDate;

	@Schema
	private Long organizationId;

	@Schema
	private OrganizationRefDTO organization;

	@Schema
	private BusinessUnitRefDTO businessUnit;

	@Schema
	private List<OrganizationRefDTO> vendors;

	@Schema
	private Boolean useMultiFactorAuth;

	@Schema
	private Set<UserMetadataRefDTO> entityMetadata;

	@Schema
	private DocumentDTO profilePicture;

	@Schema
	private Boolean removeProfilePicture;

	@Schema
	private Long logoutAfterInactivityTime;

	/**
	 * Entity based constructor
	 *
	 * @param users
	 */
	public UserDTO(Users users) {
		super(users);
	}

	/**
	 * Initialize current DTO object from Users entity
	 *
	 * @param entity
	 */
	@Override
	public void fromEntity(Users entity) {
		super.fromEntity(entity);

		// Filling User Roles List
		roleNames = new ArrayList<>();
		Optional.ofNullable(entity.getRoles()).orElse(new HashSet<>()).stream().forEach(role -> {
			if (role != null) roleNames.add(role.getName());
		});

		roles = Optional.ofNullable(entity.getRoles()).orElse(new HashSet<>()).stream().map(roles1 -> new ItemViewDTO<Roles>(roles1)).collect(Collectors.toList());

		if (entity.getOrganization() != null) {
			organizationId = entity.getOrganization().getId();
			logoutAfterInactivityTime = entity.getOrganization().getLogoutAfterInactivityTime();
		}

		if (entity.getProfilePicture() != null) {
			profilePicture = new DocumentDTO(entity.getProfilePicture(), true);
		}

		if (entity.getBusinessUnit() != null) {
			businessUnit = new BusinessUnitRefDTO(entity.getBusinessUnit());
		}

		if (UserService.isVendorEmployee(entity)) {
			vendors = Optional.ofNullable(entity.getVendors()).orElse(new HashSet<>()).stream().map(OrganizationRefDTO::new).collect(Collectors.toList());
		}

		entityMetadata = Optional.ofNullable(entity.getMetadata())
			.stream()
			.flatMap(Set::stream).map(UserMetadataRefDTO::new).collect(Collectors.toSet());
	}
}
