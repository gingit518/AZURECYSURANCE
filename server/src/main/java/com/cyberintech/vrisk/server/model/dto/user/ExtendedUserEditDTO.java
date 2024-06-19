package com.cyberintech.vrisk.server.model.dto.user;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.TwoFactorType;
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
public class ExtendedUserEditDTO extends DTOWithMetaData<Users> {

	@Schema
	private Long id;

	@Schema
	private OrganizationRefDTO organization;

	@Schema
	private String fullName;

	@Schema
	private String firstName;

	@Schema
	private String lastName;

	@Schema
	private String email;

	@Schema
	private String passwordPlain;

	@Schema
	private String title;

	@Schema
	private String corporatePhone;

	@Schema
	private String mobilePhone;

	@Schema
	private Boolean enabled;

	@Schema
	private Boolean expired;

	@Schema
	private Boolean credentialsExpired;

	@Schema
	private Boolean locked;

	@Schema
	private Date expirationDate;

	@Schema
	private Date credentialsExpirationDate;

	@Schema
	private BusinessUnitRefDTO businessUnit;

	@Schema
	private Boolean useMultiFactorAuth;

	@Schema
	private TwoFactorType twoFactorType;

	@Schema
	private List<OrganizationRefDTO> vendors;

	@Schema
	private List<IdpUserDTO> idpUsers;

	/**
	 * User role codes list
	 */
	@Schema(type = "array", name = "List of User role codes",
		allowableValues = "USER, ADMIN, CEO, CRO, CISO, CLM, DPO, REM, SYSOWN, INFOSEC, TSM, PM, BUO, AUD, RISKMAN, PRCOWN, ORGADMIN"
	)
	private List<String> roleNames;

	/**
	 * User role codes list
	 */
	@Schema(type = "array", name = "List of User role objects")
	private List<ItemViewDTO<Roles>> roles;

	@Schema
	private DocumentDTO profilePicture;

	@Schema
	private Boolean removeProfilePicture;

	/**
	 * Default constructor
	 */
	public ExtendedUserEditDTO() {
		super();
	}

	/**
	 * Entity based constructor
	 *
	 * @param users
	 */
	public ExtendedUserEditDTO(Users users) {
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

		twoFactorType = entity.getTwoFactorType();

		roleNames = Optional.ofNullable(entity.getRoles()).orElse(new HashSet<>()).stream().map(Roles::getName).collect(Collectors.toList());
		roles = Optional.ofNullable(entity.getRoles()).orElse(new HashSet<>()).stream().map(roles1 -> new ItemViewDTO<Roles>(roles1)).collect(Collectors.toList());

		if (entity.getOrganization() != null) {
			this.setOrganization(new OrganizationRefDTO(entity.getOrganization()));
		}

		if (entity.getBusinessUnit() != null) {
			this.setBusinessUnit(new BusinessUnitRefDTO(entity.getBusinessUnit()));
		}

		if (entity.getProfilePicture() != null) {
			this.setProfilePicture(new DocumentDTO(entity.getProfilePicture(), true));
		}

		if (UserService.isVendorEmployee(entity)) {
			vendors = Optional.ofNullable(entity.getVendors()).orElse(new HashSet<>()).stream().map(OrganizationRefDTO::new).collect(Collectors.toList());;
		}

		idpUsers = Optional.ofNullable(entity.getIdpUsers()).orElse(new HashSet<>()).stream().map(IdpUserDTO::new).collect(Collectors.toList());
	}

}
