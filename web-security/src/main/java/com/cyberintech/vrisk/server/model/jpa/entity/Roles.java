package com.cyberintech.vrisk.server.model.jpa.entity;

import static javax.persistence.GenerationType.IDENTITY;

import lombok.*;

import javax.persistence.*;

/**
 * Role Entity Definition
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2018-10-17
 */
@Entity
@Table(name = "roles")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = { "id", "name" })
@EqualsAndHashCode(of = { "id", "name" })
public class Roles {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "name", unique = true, nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

}
