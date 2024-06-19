package com.cyberintech.vrisk.server.model.jpa.entity;

import static javax.persistence.GenerationType.IDENTITY;

import com.cyberintech.vrisk.server.model.jpa.domains.TwoFactorType;
import lombok.*;

import javax.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * User Entity Definition
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2018-10-17
 */
@Entity
@Table(name = "users")
@AllArgsConstructor
@Setter
@Getter
@ToString(of = { "id", "email", "fullName" })
@EqualsAndHashCode(of = { "id", "email" })
public class Users {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "full_name", nullable = true, insertable = false, updatable = false)
	private String fullName;

	@Column(name = "first_name", nullable = true)
	private String firstName;

	@Column(name = "last_name", nullable = true)
	private String lastName;

	@Column(name = "email", unique = true, nullable = false)
	private String email;

	@Column(name = "password")
	private String password;

	@Column(name = "enabled")
	private Boolean enabled;

	@Column(name = "expired")
	private Boolean expired;

	@Column(name = "credentials_expired")
	private Boolean credentialsExpired;

	@Column(name = "locked")
	private Boolean locked;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "expiration_date")
	private Date expirationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "credentials_expiration_date")
	private Date credentialsExpirationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_login_date")
	private Date lastLoginDate;

	@Column(name = "is_deleted")
	private Boolean deleted;

	@ManyToMany(cascade = { CascadeType.DETACH }, fetch = FetchType.LAZY)
	@JoinTable(name = "user_roles", joinColumns = { @JoinColumn(name = "user_id") }, inverseJoinColumns = {
		@JoinColumn(name = "role_id") })
	private Set<Roles> roles = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "organization_id")
	private Organizations organization;

	@Column(name = "use_multi_factor_auth")
	private Boolean useMultiFactorAuth;

	@Column(name = "two_factor_type")
	private TwoFactorType twoFactorType;

	@Column(name = "totp_secret")
	private String totpSecret;

	@Column(name = "is_phone_verified")
	private Boolean isPhoneVerified;

	@Column(name = "is_totp_verified")
	private Boolean isTotpVerified;

	@Column(name = "mobile_phone")
	private String mobilePhone;

//	@OneToMany(cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
//	@JoinColumn(name = "user_id")
//	private Set<IdpUsers> idpUsers = new HashSet<>();

	/**
	 * Default constructor
	 */
	public Users() {
		enabled = true;
		expired = false;
		locked = false;
		credentialsExpired = false;
		deleted = false;
		useMultiFactorAuth = false;
	}

	/**
	 * Apply Enabled Flag to avoid NPE in Authorization
	 *
	 * @return
	 */
	public Boolean getEnabled() {
		return Boolean.TRUE.equals(enabled);
	}

	public String buildFullName() {
		String result = "";

		if (firstName != null) {
			result = firstName + " ";
		}

		if (lastName != null) {
			result += lastName;
		}

		result = result.trim();

		return result;
	}

}
