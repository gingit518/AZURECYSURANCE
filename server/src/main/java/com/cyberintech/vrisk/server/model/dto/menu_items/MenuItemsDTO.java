package com.cyberintech.vrisk.server.model.dto.menu_items;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.MenuItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.MenuItems;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.Column;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hint Data Object
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-04-05
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "code", "hintType"})
@EqualsAndHashCode(of = {"id", "code"}, callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuItemsDTO extends DTOBase<MenuItems> {

	@Schema
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@Column(name = "item_order")
	private Long itemOrder;

	@Column(name = "parent_id")
	private ItemViewDTO parentRef;

	@Schema
	private MenuItemType type = MenuItemType.LINK;

	@Schema
	private String code;

	@Schema
	private String name;

	@Schema
	private String icon;

	@Schema
	private String link;

	@Schema
	private Map<String, Object> permission;

	@Schema
	private Map<String, Map<String, String>> translations = new HashMap<>();

	@Schema
	private List<MenuItemsDTO> list;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public MenuItemsDTO(MenuItems entity) {
		super(entity);
	}

	@Override
	public void fromEntity(MenuItems entity) {
		// super.fromEntity(hints);

		id = entity.getId();
		organizationId = entity.getOrganizationId();
		itemOrder = entity.getItemOrder();
		if (entity.getParentId() != null) {
			parentRef = new ItemViewDTO(entity.getParentId(), null);
		}
		// parentId = entity.getParentId();
		name = entity.getName();
		code = entity.getCode();
		icon = entity.getIcon();
		link = entity.getLink();
		permission = entity.getPermission();
	}

	/**
	 * Getter for Parent ID for current item
	 *
	 * @return
	 */
	@JsonIgnore
	public Long getParentId() {
		return parentRef != null ? parentRef.getId() : null;
	}
}
