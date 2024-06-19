package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Associate Systems
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-15
 */
@Entity
@Table(name = "associate_systems")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "system", "associateVendor"})
@EqualsAndHashCode(of = {"id"})
public class AssociateSystems {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "system_id")
	private Systems system;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "associate_vendor_id")
	private AssociateVendors associateVendor;

}
