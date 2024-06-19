package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * User Assigned Vendors
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-05-15
 */
@Entity
@Table(name = "user_assigned_vendors")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "vendor", "user"})
@EqualsAndHashCode(of = {"id"})
public class UserAssignedVendor {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private Users user ;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_id")
	private Organizations vendor;

}
