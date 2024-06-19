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
@Table(name = "user_rates")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "rate_type", "rate"})
@EqualsAndHashCode(of = {"id"})
public class UserRates {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@Enumerated(EnumType.STRING)
	@Column(name = "rate_type")
	private UserRateType rateType;

	@Column(name = "rate")
	private Double rate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private Users user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "currency_id")
	private Currency currency;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "start_date")
	private Date startDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_date")
	private Date endDate;

}
