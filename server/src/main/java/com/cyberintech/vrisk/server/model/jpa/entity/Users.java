package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.TwoFactorType;
import com.cyberintech.vrisk.server.model.jpa.domains.UserEmploymentType;
import com.cyberintech.vrisk.server.model.jpa.entity.common.IMetadataAware;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * User Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2018-10-17
 */
@Entity
@Table(name = "users")
@AllArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "email", "fullName"})
@EqualsAndHashCode(of = {"id", "email"})
public class Users implements IEntityWithDates, IMetadataAware<UsersMetadata> {

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

	@Column(name = "title", nullable = true)
	private String title;

	@Column(name = "corporate_phone")
	private String corporatePhone;

	@Column(name = "mobile_phone")
	private String mobilePhone;

	@Column(name = "enabled")
	private Boolean enabled;

	@Column(name = "expired")
	private Boolean expired;

	@Column(name = "credentials_expired")
	private Boolean credentialsExpired;

	@Column(name = "locked")
	private Boolean locked;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "created_by_id")
	private Users createdBy;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "updated_by_id")
	private Users updatedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at")
	private Date updatedAt;

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

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "user_roles",
		joinColumns = {@JoinColumn(name = "user_id")},
		inverseJoinColumns = {@JoinColumn(name = "role_id")}
	)
	private Set<Roles> roles = new HashSet<>();


	@Column(name = "notes")
	private String notes;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "organization_id")
	private Organizations organization;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "business_unit_id")
	private BusinessUnits businessUnit;

	/**
	 * User Vendors for Vendor Account roles
	 */
	@ManyToMany(cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "users_to_vendors",
		joinColumns = {@JoinColumn(name = "user_id")},
		inverseJoinColumns = {@JoinColumn(name = "vendor_id")}
	)
	private Set<Organizations> vendors = new HashSet<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "user_employment_type")
	private UserEmploymentType employmentType;

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private Set<UserRates> userRates = new HashSet<>();

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

	@OneToMany(cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private Set<IdpUsers> idpUsers = new HashSet<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<UsersMetadata> metadata = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "profile_icon_id")
	private Documents profileIcon;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "profile_picture_id")
	private Documents profilePicture;

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
