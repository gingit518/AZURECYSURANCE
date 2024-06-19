package com.cyberintech.vrisk.server.model.dto.audit.items;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Roles;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.service.UserService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
@ToString(of = {"email"})
@EqualsAndHashCode(of = {"email"}, callSuper = false)
public class UserAuditDTO extends DTOBase<Users> {

	private Long id;

	private ItemViewDTO<Organizations> organization;

	private String fullName;

	private String firstName;

	private String lastName;

	private String email;

	private String title;

	private String corporatePhone;

	private String mobilePhone;

	private Boolean enabled;

	private Boolean expired;

	private Boolean credentialsExpired;

	private Boolean locked;

	private Date expirationDate;

	private Date credentialsExpirationDate;

	private List<String> roleNames;

	private List<ItemViewDTO<Organizations>> vendors;

	/**
	 * Default constructor
	 */
	public UserAuditDTO() {
		super();
	}

	/**
	 * Entity based constructor
	 *
	 * @param users
	 */
	public UserAuditDTO(Users users) {
		super(users);
	}

	/**
	 * Converts from entity to DTO
	 *
	 * @param entity
	 */
	@Override
	public void fromEntity(Users entity) {
		super.fromEntity(entity);

		roleNames = Optional.ofNullable(entity.getRoles()).orElse(new HashSet<>()).stream().map(Roles::getName).collect(Collectors.toList());
		if (UserService.isVendorEmployee(entity)) {
			vendors = Optional.ofNullable(entity.getVendors()).orElse(new HashSet<>()).stream().map(ItemViewDTO::new).collect(Collectors.toList());;
		}
		if (entity.getOrganization() != null) setOrganization(new ItemViewDTO<Organizations>(entity.getOrganization()));
	}

}
