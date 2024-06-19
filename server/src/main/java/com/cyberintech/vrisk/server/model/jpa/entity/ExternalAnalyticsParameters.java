package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * External Analytics Parameters
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since	 2021-10-13
 */
@Entity
@Table(name = "external_analytics_parameters")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name", "value"})
@EqualsAndHashCode
public class ExternalAnalyticsParameters {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@Column(name = "name")
	private String name;

	@Column(name = "value")
	private String value;

}
