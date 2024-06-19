package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * System Requirement Control Test Result Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since	 2021-08-10
 */
@Entity
@Table(name = "system_geo_parameters")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "system", "country", "state", "numberOfRecProcessed"})
@EqualsAndHashCode(of = {"id", "system", "country", "state"})
public class SystemGeoParameters {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "system_id")
	private Systems system;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "country_id")
	private Country country;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "state_id")
	private State state;

	@Column(name = "number_of_rec_processed")
	private Double numberOfRecProcessed;

}
