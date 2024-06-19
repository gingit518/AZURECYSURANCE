package com.cyberintech.vrisk.server.model.jpa.entity;

import static javax.persistence.GenerationType.IDENTITY;

import com.cyberintech.vrisk.server.model.jpa.domains.TwoFactorType;
import lombok.*;

import javax.persistence.*;

/**
 * Organization Entity Definition
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2018-11-08
 */
@Entity
@Table(name = "organizations")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = { "id", "name" })
@EqualsAndHashCode(of = { "id", "name" })
public class Organizations {

	public static final double INSURANCE_LIMIT = 500000000d;
	public static final double DEFAULT_RECORD_PRICE = 196d;

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "name", unique = true, nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "use_multi_factor_auth")
	private Boolean useMultiFactorAuth;

	@Column(name = "two_factor_type")
	private TwoFactorType twoFactorType;

}
