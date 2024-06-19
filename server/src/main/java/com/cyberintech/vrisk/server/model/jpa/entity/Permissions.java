package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Permission Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-18
 */
@Entity
@Table(name = "permissions")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"})
public class Permissions {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "name", unique = true, nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "permission_group")
	private String permissionGroup;

	@Column(name = "title")
	private String title;

	@Column(name = "url")
	private String url;

	@Column(name = "item_order")
	private Long itemOrder;

}
