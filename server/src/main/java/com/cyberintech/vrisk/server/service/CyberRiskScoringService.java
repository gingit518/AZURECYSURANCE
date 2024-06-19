package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dto.qualitative_question.ReassignScoringToUserDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.UserAssignedSystemRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserAssignedVendorRepository;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import com.cyberintech.vrisk.server.service.communication.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Cyber Risk Scoring Business Logic.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-05-15
 */
@Service
public class CyberRiskScoringService {

	@Autowired
	private UserAssignedSystemRepository userAssignedSystemRepository;

	@Autowired
	private UserAssignedVendorRepository userAssignedVendorRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private SystemsService systemsService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private VendorService vendorService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private ApplicationProperties applicationProperties;

	/**
	 * Reassign System/Vendor to another user and add record to the Assigned Items Table
	 *
	 * @param riskModelId
	 * @param reassignScoringToUserDTO
	 * @return
	 */
	public Boolean reassignToUser(Long riskModelId, ReassignScoringToUserDTO reassignScoringToUserDTO) {
		Boolean result = true;

		if (reassignScoringToUserDTO.getUser() == null || reassignScoringToUserDTO.getUser().getId() == null) {
			throw new BadRequestException("Assigned user required! Please specify valid item!", ApplicationExceptionCodes.ASSIGNED_USER_REQUIRED);
		}

		// Check Assigned User
		Users user = userService.getOrganizationUser(reassignScoringToUserDTO.getUser().getId());
		if (user == null) throw new BadRequestException("Assigned user does not exists!", ApplicationExceptionCodes.ASSIGNED_USER_NOT_EXISTS);

		if (VendorType.System.equals(reassignScoringToUserDTO.getItemType()) || VendorType.Both.equals(reassignScoringToUserDTO.getItemType())) {

			if (reassignScoringToUserDTO.getItem() == null || reassignScoringToUserDTO.getItem().getId() == null) {
				throw new BadRequestException("Assigned system required! Please specify valid item!", ApplicationExceptionCodes.ASSIGNED_SYSTEM_REQUIRED);
			}
			Systems system = systemsService.getSystemForCurrentOrganization(reassignScoringToUserDTO.getItem().getId());
			if (system == null) throw new BadRequestException("Assigned system does not exists!", ApplicationExceptionCodes.ASSIGNED_SYSTEM_NOT_EXISTS);

			// Save User Assigned System if needed
			UserAssignedSystem userAssignedSystem = userAssignedSystemRepository.findFirstByUserAndSystem(user, system);
			if (userAssignedSystem == null) {
				userAssignedSystem = new UserAssignedSystem();
				userAssignedSystem.setUser(user);
				userAssignedSystem.setSystem(system);
				userAssignedSystem = userAssignedSystemRepository.save(userAssignedSystem);
			}

			// Reset User Owner
			if (Boolean.TRUE.equals(reassignScoringToUserDTO.getReassignOwner())) {
				systemsService.changeOwner(system, user);
			}

			// Send User Assignments notification to the user
			if (applicationProperties.isEmailNotificationsEnabled()) {
				emailService.sendUserAssignment(userAssignedSystem);
			}

		} else if (VendorType.Vendor.equals(reassignScoringToUserDTO.getItemType()) || VendorType.Cloud.equals(reassignScoringToUserDTO.getItemType())) {

			if (reassignScoringToUserDTO.getItem() == null || reassignScoringToUserDTO.getItem().getId() == null) {
				throw new BadRequestException("Assigned vendor required! Please specify valid item!", ApplicationExceptionCodes.ASSIGNED_VENDOR_REQUIRED);
			}
			Organizations vendor = vendorService.getVendor(reassignScoringToUserDTO.getItem().getId());
			if (vendor == null) throw new BadRequestException("Assigned vendor does not exists!", ApplicationExceptionCodes.ASSIGNED_VENDOR_NOT_EXISTS);

			// Save User Assigned Vendor if needed
			UserAssignedVendor userAssignedVendor = userAssignedVendorRepository.findFirstByUserAndVendor(user, vendor);
			if (userAssignedVendor == null) {
				userAssignedVendor = new UserAssignedVendor();
				userAssignedVendor.setUser(user);
				userAssignedVendor.setVendor(vendor);
				userAssignedVendor = userAssignedVendorRepository.save(userAssignedVendor);
			}

			// Reset User Owner
			if (Boolean.TRUE.equals(reassignScoringToUserDTO.getReassignOwner())) {
				organizationService.changeOwner(vendor, user);
			}

			// Send User Assignments notification to the user
			if (applicationProperties.isEmailNotificationsEnabled()) {
				emailService.sendUserAssignment(userAssignedVendor);
			}
		}

		return result;
	}

}
