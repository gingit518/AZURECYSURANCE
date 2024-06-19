package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.auth.UserDetailsImpl;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.language_constants.LanguageConstantValueViewDTO;
import com.cyberintech.vrisk.server.model.dto.menu_items.MenuItemsDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.LanguageConstantScopeType;
import com.cyberintech.vrisk.server.model.jpa.domains.MenuItemType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.LanguageConstantValues;
import com.cyberintech.vrisk.server.model.jpa.entity.MenuItems;
import com.cyberintech.vrisk.server.model.jpa.entity.SupportedLanguages;
import com.cyberintech.vrisk.server.repository.jpa.LanguageConstantValueRepository;
import com.cyberintech.vrisk.server.repository.jpa.MenuItemsRepository;
import com.cyberintech.vrisk.server.repository.jpa.SupportedLanguagesRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Menu Items management Service. Implements basic menu CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-04-06
 */
@Service
@Slf4j
public class MenuItemsService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private MenuItemsRepository menuItemsRepository;

	@Autowired
	private LanguageConstantValueRepository languageConstantValueRepository;

	@Autowired
	private LanguageConstantService languageConstantService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private SupportedLanguagesRepository supportedLanguagesRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	/**
	 * Get Menu Items List
	 *
	 * @return Menu Items List
	 */
	public List<MenuItemsDTO> getList() {
		List<MenuItems> items = menuItemsRepository.findAll();

		List<MenuItemsDTO> itemDTOs = DTOBase.fromEntitiesList(items, MenuItemsDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Menu Items List by its codes
	 *
	 * @return MenuItems List
	 */
	public List<MenuItemsDTO> getList(List<String> codes, String language) {
		List<MenuItems> items = menuItemsRepository.getListByCodes(codes);

		List<MenuItemsDTO> itemDTOs = DTOBase.fromEntitiesList(items, MenuItemsDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Menu Items List by its codes
	 *
	 * @return MenuItems List
	 */
	public List<MenuItemsDTO> getSelfMenu() {

		List<MenuItemsDTO> items = null;

		if (userService.isSuperAdmin()) {
			items = getMenuForOrganization(null, null);
		} else {
			UserDetailsImpl userDetailsImpl = userService.getCurrentUser();
			Set<String> permissions = permissionService.getUserPermissionNames(userDetailsImpl.getUserId());
			items = getMenuForOrganization(organizationService.getCurrentOrganizationId(), permissions);
		}

		return items;
	}

	/**
	 * Get menu for organization
	 *
	 * @param organizationId
	 * @param permissions
	 * @return
	 */
	public List<MenuItemsDTO> getMenuForOrganization(Long organizationId, Set<String> permissions) {
		List<MenuItemsDTO> result = new ArrayList<>();

		List<String> languageCodes = Arrays.asList(LanguageConstantService.DEFAULT_LANGUAGE_CODE);
		if (organizationId != null) {
			List<SupportedLanguages> entitiesList = supportedLanguagesRepository.getListByOrganizationsId(organizationId);
			if (CollectionUtils.isNotEmpty(entitiesList)) {
				languageCodes = entitiesList.stream().map(SupportedLanguages::getCode).collect(Collectors.toList());
			}
		} else {
			languageCodes = supportedLanguagesRepository.findAllByIsPublicIsTrue().stream().map(SupportedLanguages::getCode).collect(Collectors.toList());
		}

		List<MenuItems> items = menuItemsRepository.findAllByOrganizationId(organizationId);
		if (CollectionUtils.isEmpty(items) && organizationId != null) { // Load default items for the system
			items = menuItemsRepository.findAllByOrganizationId(null);
		}

		List<MenuItemsDTO> itemDTOs = DTOBase.fromEntitiesList(items, MenuItemsDTO.class);
		Map<Long, MenuItemsDTO> menuItemMap = itemDTOs.stream().collect(Collectors.toMap(MenuItemsDTO::getId, menuItemsDTO -> menuItemsDTO));
		// Map<Long, List<MenuItemsDTO>> menuItemChildrenMap = itemDTOs.stream().collect(Collectors.groupingBy(MenuItemsDTO::getParentId));
		for (MenuItemsDTO menuItemDTO : itemDTOs) {
			boolean isAllowed = true;
			if (CollectionUtils.isNotEmpty(permissions)) {
				// Applying permissions for the user
				if (menuItemDTO.getPermission() != null && menuItemDTO.getPermission().containsKey("value")) {
					Object permissionsValue = menuItemDTO.getPermission().get("value");
					List<String> itemPermissions = null;
					if (permissionsValue instanceof List) {
						itemPermissions = (List<String>) permissionsValue;
					} else if (permissionsValue instanceof String) {
						itemPermissions = Arrays.asList((String) permissionsValue);
					}

					if (CollectionUtils.isNotEmpty(itemPermissions)) {
						isAllowed = false;
						for (String permissionCode : itemPermissions) {
							if (permissions.contains(permissionCode)) {
								isAllowed = true;
								break;
							}
						}
					}
				}
			}

			if (isAllowed) {
				if (menuItemDTO.getParentId() != null) {
					if (menuItemMap.containsKey(menuItemDTO.getParentId())) {
						MenuItemsDTO parentItemDTO = menuItemMap.get(menuItemDTO.getParentId());
						parentItemDTO.setType(MenuItemType.MENU);
						if (parentItemDTO.getList() == null) {
							parentItemDTO.setList(new ArrayList<>());
						}
						parentItemDTO.getList().add(menuItemDTO);
					}
				} else {
					result.add(menuItemDTO);
				}

				for (String languageCode : languageCodes) {
					if (LanguageConstantService.menuItemsLanguageConstants.containsKey(languageCode)) {
						String label = LanguageConstantService.getMenuLanguageConstantValue(menuItemDTO.getName(), languageCode);
						Map<String, String> transaltionMap = new HashMap<>();
						transaltionMap.put("name", label);
						menuItemDTO.getTranslations().put(languageCode, transaltionMap);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Get MenuItems List
	 *
	 * @return Menu Items List
	 */
	public FilteredResponse<NameFilter, MenuItemsDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<MenuItems> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, MenuItemsDTO> filteredResponse = new FilteredResponse<NameFilter, MenuItemsDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		items = menuItemsRepository.getListByName(namePattern, filteredRequest.toPageRequest());
		count = menuItemsRepository.getCountByName(namePattern);

		List<MenuItemsDTO> itemsDTOList = DTOBase.fromEntitiesList(items, MenuItemsDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Menu Item details
	 *
	 * @return Menu Item Details
	 */
	public MenuItems getItem(Long itemId) {
		MenuItems itemDetails;

		try {
			itemDetails = menuItemsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Menu Item not found in the database [{0}]", itemId));
		}

		return itemDetails;
	}

	/**
	 * Get Menu Item DTO details
	 *
	 * @return Menu Item Details
	 */
	public MenuItemsDTO getDetails(Long itemId) {

		MenuItems itemDetails = getItem(itemId);

		MenuItemsDTO result = new MenuItemsDTO(itemDetails);

		List<LanguageConstantValues> languageConstantValues = new ArrayList<>();
		List<String> ignoredLanguages = Arrays.asList(LanguageConstantService.TEST_LANGUAGE_CODE);
		languageConstantValues = languageConstantValueRepository.getListByConstantCodes(Arrays.asList(itemDetails.getName()), ignoredLanguages);

		// Prepare Translations List
		List<LanguageConstantValueViewDTO> translations = languageConstantValues.stream().map(LanguageConstantValueViewDTO::new).collect(Collectors.toList());
		// result.setLanguageConstants(translations);

		// fill language constant
		for (LanguageConstantValueViewDTO languageConstant : translations) {
			if (!result.getTranslations().containsKey(languageConstant.getLanguage().getCode())) result.getTranslations().put(languageConstant.getLanguage().getCode(), new HashMap<>());

			Map<String, String> codesMap = result.getTranslations().get(languageConstant.getLanguage().getCode());
			String localCode = "";
			if (result.getName().equals(languageConstant.getLanguageConstant().getName())) localCode = "name";

			codesMap.put(localCode, StringUtils.isNotEmpty(languageConstant.getValue()) ? languageConstant.getValue() : languageConstant.getDefaultValue());
		}

		return result;
	}

	/**
	 * Import menu items from the JSON Array
	 *
	 * @return New Technology Categorie
	 */
	public List<MenuItemsDTO> importFromLinks(List<MenuItemsDTO> itemDetailsList, Long parentId, Long organizationId) {
		List<MenuItemsDTO> result = new ArrayList<>();

		// languageConstantService.reloadMenuLanguageConstants();
		List<SupportedLanguages> allLanguages = supportedLanguagesRepository.findAll();

		int order = 0;
		for (MenuItemsDTO importDTO : itemDetailsList) {
			String code = StringUtils.isNotEmpty(importDTO.getCode()) ? importDTO.getCode() : importDTO.getName();
			code = buildCode(code);

			if (StringUtils.isEmpty(code)) {
				log.warn(String.format("Empty code for the item: %s", importDTO.getLink()));
				continue;
			}

			order++;
			// Verify Menu Item with such name not exists
			MenuItemsDTO itemDTO;
			MenuItemsDTO savedItemDTO;
			Optional<MenuItems> existingItemOptional = menuItemsRepository.findFirstByCodeAndIdNotIn(code, Arrays.asList(0l));
			if (!existingItemOptional.isPresent()) {

				itemDTO = new MenuItemsDTO();
				if (parentId != null) itemDTO.setParentRef(new ItemViewDTO(parentId, null));
				itemDTO.setItemOrder(importDTO.getItemOrder() != null ? importDTO.getItemOrder() : order);
				itemDTO.setOrganizationId(organizationId);
				itemDTO.setCode(code);
				itemDTO.setIcon(importDTO.getIcon());
				itemDTO.setLink(importDTO.getLink());
				itemDTO.setPermission(importDTO.getPermission());
				// itemDTO.setName(label);

				// Build Translations List
				for (SupportedLanguages  language : allLanguages) {
					if (LanguageConstantService.uiLanguageConstants.containsKey(language.getCode()) && LanguageConstantService.uiLanguageConstants.get(language.getCode()).containsKey(importDTO.getName())) {
						Map<String, String> translations = new HashMap<>();
						translations.put("name", LanguageConstantService.uiLanguageConstants.get(language.getCode()).get(importDTO.getName()));
						itemDTO.getTranslations().put(language.getCode(), translations);
					}
				}

				savedItemDTO = create(itemDTO);
				result.add(savedItemDTO);

			} else {
				MenuItems existingItem = existingItemOptional.get();
				existingItem.setParentId(parentId);
				existingItem.setItemOrder(importDTO.getItemOrder() != null ? importDTO.getItemOrder() : order);
				existingItem.setOrganizationId(organizationId);
				existingItem.setPermission(importDTO.getPermission());

				MenuItemsDTO existingItemDTO = new MenuItemsDTO(existingItem);
				savedItemDTO = update(existingItemDTO);
				result.add(savedItemDTO);
			}

			// Set child nodes
			if (CollectionUtils.isNotEmpty(importDTO.getList())) {
				List<MenuItemsDTO> children = importFromLinks(importDTO.getList(), savedItemDTO.getId(), organizationId);
				savedItemDTO.setList(children);
			}
		}

		// languageConstantService.reloadMenuLanguageConstants();

		return result;
	}

	/**
	 * Create new Menu Item Domain
	 *
	 * @return New Technology Categorie
	 */
	public MenuItemsDTO create(MenuItemsDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		String name = newItemDTO.getName();

//		MenuItems newItem = newItemDTO.toEntity();
		MenuItems newItem = new MenuItems();
		applyEntityChanges(newItemDTO, newItem);
		MenuItems saveResult = menuItemsRepository.save(newItem);

		MenuItemsDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.MENU_ITEM,
			saveResult.getId(),
			result,
			null
		);

		return result;
	}

	/**
	 * Update Technology Categorie
	 *
	 * @return Updated Qualitative Domains
	 */
	public MenuItemsDTO update(MenuItemsDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();
		Boolean isSuperAdmin = userService.isSuperAdmin();

		// Get Existing item from the database
		MenuItems existingItem = getItem(itemDTO.getId());
		MenuItemsDTO existingItemDTO = new MenuItemsDTO(existingItem);

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		MenuItems saveResult = menuItemsRepository.save(existingItem);

		MenuItemsDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.MENU_ITEM,
			saveResult.getId(),
			existingItemDTO,
			result,
			null
		);

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(MenuItemsDTO itemDTO, MenuItems entity) {

		String code = buildCode(itemDTO.getCode());

		if (StringUtils.isEmpty(code)) {
			throw new BadRequestException(MessageFormat.format("Menu Item code cannot be empty {0}", code), ApplicationExceptionCodes.HINT_CODE_EMPTY);
		}

		// Verify Menu Item with such name not exists
		if (menuItemsRepository.findFirstByCodeAndIdNotIn(code, Arrays.asList(entity.getId() != null ? entity.getId() : 0l)).isPresent()) {
			throw new ConflictException(MessageFormat.format("Menu Item with this name already exist {0}", code), ApplicationExceptionCodes.HINT_ALREADY_EXISTS);
		}

		// Apply entity changes
		entity.setCode(code);
		entity.setIcon(itemDTO.getIcon());
		if (itemDTO.getParentRef() != null) entity.setParentId(itemDTO.getParentRef().getId());
		if (StringUtils.isEmpty(itemDTO.getName())) {
			entity.setName(buildNameCode(code));
		} else {
			entity.setName(buildNameCode(buildCode(itemDTO.getName())));
		}
		entity.setLink(itemDTO.getLink());
		entity.setPermission(itemDTO.getPermission());

		// Save translations
		boolean isLanguageUpdated = false;
		if (itemDTO.getTranslations() != null && itemDTO.getTranslations().size() > 0) {
			for (Map.Entry<String, Map<String, String>> langInfoMapEntry : itemDTO.getTranslations().entrySet()) {
				String languageCode = langInfoMapEntry.getKey();
				Optional<SupportedLanguages> language = supportedLanguagesRepository.findFirstByCode(languageCode);
				if (language.isPresent()) {
					HashMap<String, String> codesMap = new HashMap<>();
					if (langInfoMapEntry.getValue().containsKey("name")) codesMap.put(entity.getName(), langInfoMapEntry.getValue().get("name"));

					languageConstantService.importLanguageConstantsFromStringMap(language.get(), LanguageConstantScopeType.MENU, codesMap);
					isLanguageUpdated = true;
				}
			}
		}

		// Reloading MenuItems language constants
		if (isLanguageUpdated) {
			// languageConstantService.reloadMenuLanguageConstants();
		}
	}

	/**
	 * Build code variable name
	 *
	 * @param code
	 * @return
	 */
	public static String buildCode(String code) {
		String result = StringUtils.substring(code.replaceAll("MENU\\$", "").replaceAll("\\$TITLE", ""), 0, 63);
		result = result.replaceAll("MENU\\.", "").replaceAll("\\.LABEL", "");
		result = result.replaceAll("[\\$\\.]", "_").replaceAll("[^a-zA-Z0-9\\.\\_]", "").toUpperCase();

		return result;
	}

	/**
	 * Build code for title variable
	 * @param code
	 * @return
	 */
	public static String buildNameCode(String code) {
		return "MENU." + code + ".LABEL";
	}

	/**
	 * Deletes Menu Items
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		MenuItems existingItem = getItem(itemId);
		MenuItemsDTO existingItemDTO = new MenuItemsDTO(existingItem);
		menuItemsRepository.delete(existingItem);
		menuItemsRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.MENU_ITEM,
			existingItemDTO.getId(),
			existingItemDTO,
			null
		);

		return itemId;
	}

}
