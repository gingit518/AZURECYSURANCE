package com.cyberintech.vrisk.server.model.auth;

import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Default Spring user abstraction
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-10-26
 */
public class UserDetailsImpl extends User {

	private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

	@Getter
	private Long userId;

	@Getter
	private Long organizationId;

	/**
	 * Fully parametrized constructor
	 *
	 * @param userId
	 * @param username
	 * @param password
	 * @param enabled
	 * @param authorities
	 */
	public UserDetailsImpl(Long userId, String username, String password, boolean enabled, Collection<? extends GrantedAuthority> authorities) {
		super(username, password, enabled, true, true, true, authorities);

		this.userId = userId;
	}

	public UserDetailsImpl(UserDetails userDetails, Collection<? extends GrantedAuthority> authorities, Long userId) {
		this(userId, userDetails.getUsername(), userDetails.getPassword(), userDetails.isEnabled(), authorities);

		if (userDetails instanceof UserDetailsImpl) {
			organizationId = ((UserDetailsImpl) userDetails).organizationId;
		}
	}

	/**
	 * Default User Entity constructor
	 *
	 * @param user
	 */
	public UserDetailsImpl(Users user) {
		this(Long.valueOf(user.getId()), user.getEmail(), user.getPassword(), user.getEnabled(), AuthorityUtils.NO_AUTHORITIES);

		if (user.getOrganization() != null) {
			organizationId = user.getOrganization().getId();
		}
	}

	public static UserDetailsImpl of(Users user) {
		List<GrantedAuthority> authorities = user.getRoles().stream()
			.map(rolesEntity -> new SimpleGrantedAuthority(rolesEntity.getName()))
			.collect(Collectors.toList());
		UserDetailsImpl userDetails = new UserDetailsImpl(Long.valueOf(user.getId()), user.getEmail(), Optional.ofNullable(user.getPassword()).orElse(""), user.getEnabled(), authorities);
		if (user.getOrganization() != null) {
			userDetails.organizationId = user.getOrganization().getId();
		}

		return userDetails;
	}

	/**
	 * Apply/Override Organization ID for User
	 *
	 * @param organizationId
	 */
	public void applyOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

}
