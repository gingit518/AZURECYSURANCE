package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.UserRateType;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * User Rates Entity
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-12-03
 */
@Entity
@Table(name = "api_keys")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "organization_id", "apiKeyPublic"})
@EqualsAndHashCode(of = {"id"})
public class ApiKeys {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private Users user;

	@Column(name = "api_key_private")
	private String apiKeyPrivate;

	@Column(name = "api_key_public")
	private String apiKeyPublic;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "expired_at")
	private Date expiredAt;

}
