package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.HintType;
import com.cyberintech.vrisk.server.model.jpa.entity.converters.MapOfObjectsConverter;
import lombok.*;

import javax.persistence.*;
import java.util.Map;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Menu Items Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-04-06
 */
@Entity
@Table(name = "menu_items")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "organization_id", "code", "name"})
@EqualsAndHashCode(of = {"id"})
public class MenuItems {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@Column(name = "parent_id")
	private Long parentId;

	@Column(name = "item_order")
	private Long itemOrder;

	@Column(name = "name")
	private String name;

	@Column(name = "code", unique = true, nullable = false)
	private String code;

	@Column(name = "icon")
	private String icon;

	@Column(name = "link")
	private String link;

	@SuppressWarnings("JpaAttributeTypeInspection")
	@Column(name = "permission")
	@Convert(converter = MapOfObjectsConverter.class)
	private Map<String, Object> permission;

}
