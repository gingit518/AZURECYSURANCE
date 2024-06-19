package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.menu_items.MenuItemsDTO;
import com.cyberintech.vrisk.server.service.LanguageConstantService;
import com.cyberintech.vrisk.server.service.MenuItemsService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Menu Items management controller. Basic menu CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-04-07
 */
@RestController
@RequestMapping(
	value = MenuItemsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Menu Items Management Controller"
)
@Tag(name = "Menu Items Management")
public class MenuItemsController {

	static final String CONTROLLER_URI = "/api/menu";

	@Autowired
	private MenuItemsService menuItemsService;

	@Autowired
	private LanguageConstantService languageConstantService;

	/**
	 * Get Menu Items List for organization
	 *
	 * @return Menu Items List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/organization", name = "Menu Items List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<MenuItemsDTO> getListForCodes(@Parameter(example = "") @RequestHeader("organization-id") @Nullable Long organizationId) {

		List<MenuItemsDTO> result = menuItemsService.getMenuForOrganization(organizationId, null);

		return result;
	}

	/**
	 * Get Menu Items List for current Risk Model
	 *
	 * @return Menu Items List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Menu Items List for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).MENU_READ)")
	public FilteredResponse<NameFilter, MenuItemsDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, MenuItemsDTO> result = menuItemsService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Menu Item details
	 *
	 * @return Menu Item Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Menu Item details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).MENU_READ)")
	public MenuItemsDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		MenuItemsDTO itemDTO = menuItemsService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Menu Item
	 *
	 * @return New Menu Item
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Menu Item", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).MENU_CREATE)")
	public MenuItemsDTO create(
		@Parameter(description = "Menu Item Details", required = true) @RequestBody MenuItemsDTO newItemDTO
	) {

		MenuItemsDTO result = menuItemsService.create(newItemDTO);

		// Reloading Menu Items Translations
		languageConstantService.reloadMenuLanguageConstants();

		return result;
	}

	/**
	 * Create new Menu Item
	 *
	 * @return New Menu Item
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/import", name = "Create new Menu Item", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).MENU_CREATE)")
	public List<MenuItemsDTO> importFromJson(
		@Parameter(description = "Menu Item Details", required = true) @RequestBody List<MenuItemsDTO> menuConfig
		, @Parameter(example = "") @RequestHeader("organization-id") @Nullable Long organizationId
	) {

		List<MenuItemsDTO> result = menuItemsService.importFromLinks(menuConfig, null, organizationId);

		// Reloading Menu Items Translations
		languageConstantService.reloadMenuLanguageConstants();

		return result;
	}

	/**
	 * Update Menu Item
	 *
	 * @return Updated Menu Item
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Menu Item", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).MENU_UPDATE)")
	public MenuItemsDTO update(
		@Parameter(description = "Menu Items update Details", required = true) @RequestBody MenuItemsDTO itemDTO
	) {

		MenuItemsDTO result = menuItemsService.update(itemDTO);

		// Reloading Menu Items Translations
		languageConstantService.reloadMenuLanguageConstants();

		return result;
	}

	/**
	 * Deletes Menu Item
	 *
	 * @return ID of removed Menu Item
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Menu Item", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).MENU_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Menu Item Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = menuItemsService.delete(itemDTO.getId());

		return result;
	}

}
