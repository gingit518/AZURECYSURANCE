package com.cyberintech.vrisk.server.model.dto.user;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.SLCT;
import com.cyberintech.vrisk.server.model.jpa.entity.Roles;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.jpa.RoleRepository;
import com.cyberintech.vrisk.server.util.BeanUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
@ToString(of = { "email" })
@EqualsAndHashCode(of = { "email" }, callSuper = false)
public class UserCreateDTO extends DTOBase<Users> {

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
	@Size(max = 20, message = SLCT.VALIDATION$USER$CORPORATE_PHONE$MAX_LENGTH)
	private String corporatePhone;

	@Schema
	@NotBlank(message = SLCT.GLOBALS$MOBILE_PHONE_REQUIRED)
	@Size(max = 20, message = SLCT.VALIDATION$USER$MOBILE_PHONE$MAX_LENGTH)
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
	@Schema(type = "array", name = "List of User role codes", allowableValues = "USER, ADMIN")
	private List<String> roleNames;

	@Schema
	private Boolean credentialsExpired;

	@Schema
	private Boolean locked;

	@Schema
	@FutureOrPresent(message = SLCT.VALIDATION$USER$EXPIRATION_DATE$IN_PAST)
	private Date expirationDate;

	@Schema
	@FutureOrPresent(message = SLCT.VALIDATION$USER$CREDENTIALS_EXPIRATION_DATE$IN_PAST)
	private Date credentialsExpirationDate;

	@Schema
	private BusinessUnitRefDTO businessUnit;

	@Schema
	private List<OrganizationRefDTO> vendors;

	@Schema
	private Boolean useMultiFactorAuth;

	public UserCreateDTO() {
		super();
	}

	public UserCreateDTO(Users entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Users users) {
		super.fromEntity(users);

		// Filling User Roles List
		roleNames = new ArrayList<>();
		Optional.ofNullable(users.getRoles()).orElse(new HashSet<>()).stream().forEach(role -> {
			roleNames.add(role.getName());
		});

		roles = Optional.ofNullable(users.getRoles()).orElse(new HashSet<>()).stream()
			.map(roles1 -> new ItemViewDTO<Roles>(roles1)).collect(Collectors.toList());

	}

	/**
	 * Convert User Details DTO to Entity
	 *
	 * @return
	 */
	@Override
	public Users toEntity(Users update) {

		Users result = update;

		update.setFirstName(firstName);
		update.setLastName(lastName);
		update.setEmail(email);
		update.setCorporatePhone(corporatePhone);
		update.setMobilePhone(mobilePhone);
		update.setTitle(title);

		RoleRepository roleRepository = BeanUtil.getBean(RoleRepository.class);
		PasswordEncoder passwordEncoder = BeanUtil.getBean(PasswordEncoder.class);

		// Set Encoded Password if it is defined
		if (StringUtils.isNotEmpty(passwordPlain)) {
			result.setPassword(passwordEncoder.encode(passwordPlain));
		}

		// Set Roles List from objects or names
		if (getRoles() != null) {
			Optional.ofNullable(getRoles()).ifPresent(rolesList -> {
				result.setRoles(new HashSet<>());
				getRoles().stream().forEach(role -> {
					result.getRoles().add(roleRepository.findById(role.getId()).get());
				});
			});
		} else {
			Optional.ofNullable(roleNames).ifPresent(rolesList -> {
				result.setRoles(new HashSet<>());
				roleNames.stream().forEach(roleName -> {
					result.getRoles().add(roleRepository.findOneByName(roleName));
				});
			});
		}

		// Set Create/Update Dates
		if (result.getCreatedAt() == null) {
			result.setCreatedAt(new Date());
		}
		result.setUpdatedAt(new Date());

		return result;
	}

}
