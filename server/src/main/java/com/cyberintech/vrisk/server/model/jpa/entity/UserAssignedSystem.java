package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * User Assigned Systems
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-05-15
 */
@Entity
@Table(name = "user_assigned_systems")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "system", "user"})
@EqualsAndHashCode(of = {"id"})
public class UserAssignedSystem {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private Users user ;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "system_id")
	private Systems system;


}
