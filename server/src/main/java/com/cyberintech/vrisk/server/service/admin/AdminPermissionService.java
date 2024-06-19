package com.cyberintech.vrisk.server.service.admin;

import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.role.*;
import com.cyberintech.vrisk.server.model.jpa.domains.RoleType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.Permissions;
import com.cyberintech.vrisk.server.model.jpa.entity.Roles;
import com.cyberintech.vrisk.server.repository.jpa.PermissionRepository;
import com.cyberintech.vrisk.server.repository.jpa.RoleRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import com.cyberintech.vrisk.server.rest.exception.InternalServerErrorException;
import com.cyberintech.vrisk.server.service.AuditLogService;
import com.cyberintech.vrisk.server.service.utils.CSVUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin Permissions Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-18
 */
@Service
@Slf4j
public class AdminPermissionService {

	public static final String PERMISSION_GROUP = "Permission Group";
	public static final String PERMISSION_TITLE = "Permission Title";
	public static final String PERMISSION_ROLE = "Permissions/Roles";
	public static final String PERMISSION_URL = "Url";
	public static final String PERMISSION_DESCRIPTION = "Description";

	public static final Set<String> ALL_CSV_HEADERS_SET = Arrays.asList(PERMISSION_GROUP, PERMISSION_ROLE, PERMISSION_TITLE, PERMISSION_URL, PERMISSION_DESCRIPTION).stream().collect(Collectors.toSet());

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private PermissionRepository permissionRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRepository userRepository;

	/**
	 * Get All Permission Roles
	 *
	 * @return
	 */
	public List<PermissionRolesDTO> getAllPermissionRoles() {
		List<Roles> rolesList = IterableUtils.toList(roleRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
		List<Permissions> permissionsList = permissionRepository.getAllOrderedByItemOrder();

		List<PermissionRolesDTO> result = PermissionRolesDTO.fromEntitiesList(permissionsList, PermissionRolesDTO.class);
		Map<Long, RoleListDTO> rolesMap = RoleListDTO.fromEntitiesList(rolesList, RoleListDTO.class).stream().collect(Collectors.toMap(RoleListDTO::getId, roleListDTO -> roleListDTO));

		Map<String, PermissionRolesDTO> permissionsMap = result.stream().collect(Collectors.toMap(PermissionRolesDTO::getName, permissionRolesDTO -> permissionRolesDTO));
		for (Roles role : rolesList) {
			for (Permissions permission : role.getPermissions()) {
				if (permissionsMap.containsKey(permission.getName())) {
					PermissionRolesDTO currentPermission = permissionsMap.get(permission.getName());
					currentPermission.getRoles().add(rolesMap.get(role.getId()));
				}
			}

			// Apply All permissions to ADMIN user
			if (RoleType.ADMIN.role().equals(role.getName())) {
				for (Map.Entry<String, PermissionRolesDTO> permissionEntry : permissionsMap.entrySet()) {
					permissionEntry.getValue().getRoles().add(rolesMap.get(role.getId()));
				}
			}
		}

		return result;
	}

	/**
	 * Update Role Permission Data
	 *
	 * @return
	 */
	@Transactional
	public List<RolePermissionUpdateDTO> updateRolePermissionData(List<RolePermissionUpdateDTO> permissionItems) {
		List<RolePermissionUpdateDTO> result = new ArrayList<>();

		for (RolePermissionUpdateDTO rolePermissionUpdate : permissionItems) {
			Optional<Roles> roleOptional = roleRepository.findById(rolePermissionUpdate.getRole().getId());
			Optional<Permissions> permissionOptional = permissionRepository.findById(rolePermissionUpdate.getPermission().getId());

			if (roleOptional.isPresent() && permissionOptional.isPresent()) {
				Roles role = roleOptional.get();
				Permissions permission = permissionOptional.get();

				if (rolePermissionUpdate.isAllowed()) {
					if (!role.getPermissions().contains(permission)) {
						role.getPermissions().add(permission);

						// Save Audit Log CREATE event
						auditLogService.create(
							VItemType.ROLE_PERMISSIONS,
							permission.getId(),
							RolePermissionLogDTO.of(new PermissionRefDTO(permission), new RoleListDTO(role), true),
							null
						);
					}
				} else {
					if (role.getPermissions().contains(permission)) {
						role.getPermissions().remove(permission);

						// Save Audit Log Delete event
						auditLogService.delete(
							VItemType.ROLE_PERMISSIONS,
							permission.getId(),
							RolePermissionLogDTO.of(new PermissionRefDTO(permission), new RoleListDTO(role), false),
							null
						);
					}
				}

				roleRepository.save(role);
				result.add(rolePermissionUpdate);
			}
		}

		return result;
	}

	/**
	 * Get Template content for Download
	 */
	public ByteArrayInputStream getDownloadData() {

		// String templateContent = "Business Unit Name,Business Unit Description,Parent Business Unit";
		ByteArrayInputStream byteArrayInputStream = null;

		try {
			List<String> headersList = new ArrayList<>();
			headersList.add(PERMISSION_GROUP);
			headersList.add(PERMISSION_TITLE);
			headersList.add(PERMISSION_ROLE);
			headersList.add(PERMISSION_DESCRIPTION);
			headersList.add(PERMISSION_URL);

			List<Roles> rolesList = IterableUtils.toList(roleRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
			List<Permissions> permissionsList = permissionRepository.getAllOrderedByItemOrder();
			Map<Permissions, Set<Roles>> permissionsRolesSetMap = new HashMap<>();
			for (Roles role : rolesList) {
				for (Permissions permission : role.getPermissions()) {
					if (!permissionsRolesSetMap.containsKey(permission)) {
						permissionsRolesSetMap.put(permission, new HashSet<>());
					}

					permissionsRolesSetMap.get(permission).add(role);
				}

				headersList.add(role.getName());
			}

			String[] headers = headersList.toArray(new String[0]);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			Writer writer = new OutputStreamWriter(outputStream);
			CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(headers);

			CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);
			for (Permissions permission : permissionsList) {
				List<String> itemsList = new ArrayList<>();
				itemsList.add(permission.getPermissionGroup());
				itemsList.add(permission.getTitle());
				itemsList.add(permission.getName());
				itemsList.add(permission.getDescription());
				itemsList.add(permission.getUrl());

				for (Roles role : rolesList) {
					String permissionSetFlag = permissionsRolesSetMap.containsKey(permission) && permissionsRolesSetMap.get(permission).contains(role) ? "Y" : "";
					if (role.getId().equals(1l)) {
						permissionSetFlag = "Y";
					}
					itemsList.add(permissionSetFlag);
				}

				csvPrinter.printRecord(itemsList);
			}
			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			log.warn(e.getMessage(), e);
			throw new InternalServerErrorException("Failed to generate CSV Data file");
		}

		return byteArrayInputStream;
	}

	/**
	 * Import permissions data from CSV file
	 */
	@Transactional
	public ImportResultDTO importFromCSVFile(MultipartFile file) {
		return importFromCSVFile(file, null);
	}

	/**
	 * Import permissions data from CSV file
	 */
	@Transactional
	public ImportResultDTO importFromCSVFile(MultipartFile file, Boolean forceSynchronize) {

		ImportResultDTO result = new ImportResultDTO();

		try {

			List<Permissions> permissionsList = permissionRepository.getAllOrderedByItemOrder();
			Map<String, Permissions> permissionsUnsyncMap = permissionsList.stream().collect(Collectors.toMap(Permissions::getName, permissions -> permissions));

			// Parse CSV file
			CSVParser csvParser = CSVUtils.createCSVParser(file.getInputStream());
			List<CSVRecord> csvRecordList = csvParser.getRecords();

			// Produce Headers
			Map<String, Integer> headersMap = csvParser.getHeaderMap();
			Map<Integer, Roles> rolesMap = new HashMap<>();
			for (Map.Entry<String, Integer> entry : headersMap.entrySet()) {
				String roleName = entry.getKey();
				// if (!Integer.valueOf(0).equals(entry.getValue())) {
				if (!ALL_CSV_HEADERS_SET.contains(roleName)) {
					Roles role = roleRepository.findOneByNameIgnoreCase(roleName);

					if (role != null) {
						rolesMap.put(entry.getValue(), role);
					} else if (Boolean.TRUE.equals(forceSynchronize)) {
						Roles newRole = new Roles();
						newRole.setName(roleName);
						role = roleRepository.save(newRole);
						rolesMap.put(entry.getValue(), role);
					}
				}
			}

			// Build Permissions Map
			Map<String, Permissions> permissionsMap = new HashMap<>();
			long itemOrder = 0;
			for (CSVRecord record : csvRecordList) {
				String permissionName = record.get(PERMISSION_ROLE).trim();
				String permissionGroup = record.isMapped(PERMISSION_GROUP) ? record.get(PERMISSION_GROUP).trim() : null;
				String permissionTitle = record.isMapped(PERMISSION_TITLE) ? record.get(PERMISSION_TITLE).trim() : null;
				String permissionDescription = record.isMapped(PERMISSION_DESCRIPTION) ? record.get(PERMISSION_DESCRIPTION).trim() : null;
				String permissionUrl = record.isMapped(PERMISSION_URL) ? record.get(PERMISSION_URL).trim() : null;
				String permissionNameLower = permissionName.toLowerCase();
				Permissions permission = permissionRepository.findOneByNameIgnoreCase(permissionNameLower);

				PermissionDTO originalPermissionDTO = null;

				if (permission == null) {
					permission = new Permissions();
					permission.setName(permissionName);

					log.info(MessageFormat.format("## Create permission for name [{0}, {1}, {2}, {3}]", permissionName, itemOrder, permissionGroup, permissionTitle));
				} else {
					originalPermissionDTO = new PermissionDTO(permission);
					log.info(MessageFormat.format("## Update permission for name [{0}, {1}, {2}, {3}]", permissionName, itemOrder, permissionGroup, permissionTitle));
				}

				if (StringUtils.isNotEmpty(permissionGroup)) {
					permission.setPermissionGroup(permissionGroup);
				}
				if (StringUtils.isNotEmpty(permissionTitle)) {
					permission.setTitle(permissionTitle);
				}
				if (StringUtils.isNotEmpty(permissionDescription)) {
					permission.setDescription(permissionDescription);
				}
				if (StringUtils.isNotEmpty(permissionUrl)) {
					permission.setUrl(permissionUrl);
				}
				permission.setItemOrder(++itemOrder);

				permission = permissionRepository.save(permission);

				permissionsMap.put(permissionNameLower, permission);

				if (permissionsUnsyncMap.containsKey(permissionName)) {
					permissionsUnsyncMap.remove(permissionName);
				}

				if (originalPermissionDTO != null) {
					PermissionDTO updatedPermission = new PermissionDTO(permission);
					if (!updatedPermission.equals(originalPermissionDTO)) {
						// Save Audit Log UPDATE event
						auditLogService.update(
							VItemType.PERMISSIONS,
							permission.getId(),
							originalPermissionDTO,
							updatedPermission,
							null
						);
					}
				} else {
					// Save Audit Log CREATE event
					auditLogService.create(
						VItemType.PERMISSIONS,
						permission.getId(),
						new PermissionDTO(permission),
						null
					);
				}
			}

			// Setup Permissions
			for (CSVRecord record : csvRecordList) {
				String permissionNameLower = record.get(PERMISSION_ROLE).trim().toLowerCase();
				Permissions permission = permissionsMap.get(permissionNameLower);

				if (permission != null) {
					for (Map.Entry<Integer, Roles> roleMapEntry : rolesMap.entrySet()) {
						Integer roleIndex = roleMapEntry.getKey();
						Roles role = roleMapEntry.getValue();

						String itemValue = record.get(roleIndex);
						boolean isPermissionsSet = (itemValue != null) && ("y".equalsIgnoreCase(itemValue.trim()) || "yes".equalsIgnoreCase(itemValue.trim()));
						if (isPermissionsSet) {
							if (!role.getPermissions().contains(permission)) {
								role.getPermissions().add(permission);
								log.info(MessageFormat.format("## Adding permission [{0}] for role [{1}]", permission.getName(), role.getName()));

								// Save Audit Log CREATE event
								auditLogService.create(
									VItemType.ROLE_PERMISSIONS,
									permission.getId(),
									RolePermissionLogDTO.of(new PermissionRefDTO(permission), new RoleListDTO(role), true),
									null
								);
							}
						} else {
							if (role.getPermissions().contains(permission)) {
								role.getPermissions().remove(permission);
								log.info(MessageFormat.format("  ## Removing permission [{0}] from role [{1}]", permission.getName(), role.getName()));

								// Save Audit Log DELETE event
								auditLogService.delete(
									VItemType.ROLE_PERMISSIONS,
									permission.getId(),
									RolePermissionLogDTO.of(new PermissionRefDTO(permission), new RoleListDTO(role), false),
									null
								);
							}
						}
					}
				}
			}

			// Save Role
			for (Map.Entry<Integer, Roles> roleMapEntry : rolesMap.entrySet()) {
				final Roles role = roleMapEntry.getValue();
				roleRepository.save(role);
			}

			log.info("Finished import permissions");

			if (Boolean.TRUE.equals(forceSynchronize) && permissionsUnsyncMap.size() > 0) {
				for (Permissions permission : permissionsUnsyncMap.values()) {
					permissionRepository.delete(permission);

					// Save Audit Log Delete event
					auditLogService.delete(
						VItemType.PERMISSIONS,
						permission.getId(),
						new PermissionDTO(permission),
						null
					);
				}
			}

		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}

		return result;
	}

}
