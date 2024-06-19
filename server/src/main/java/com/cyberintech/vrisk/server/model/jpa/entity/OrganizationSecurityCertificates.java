package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.ExternalAnalyticsType;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Organization Security Certificate Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-10-25
 */
@Entity
@Table(name = "organization_security_certificates")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
public class OrganizationSecurityCertificates {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id", insertable = false, updatable = false)
	private Organizations organization;

	@Column(name = "user_id")
	private Long userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", insertable = false, updatable = false)
	private Users user;

	@Enumerated(EnumType.STRING)
	@Column(name = "organization_security_certificates_type")
	private ExternalAnalyticsType certificateType;

	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "base_url")
	private String baseUrl;

	@Column(name = "client_id")
	private String cliendId;

	@Column(name = "client_secret")
	private String clientSecret;

	@Column(name = "private_key")
	private String privateKey;

	@Column(name = "public_key")
	private String publicKey;

	@Column(name = "is_active")
	private Boolean isActive;

}
