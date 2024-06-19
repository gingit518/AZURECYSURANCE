package com.cyberintech.vrisk.server.service.communication;

import com.cyberintech.vrisk.server.model.jpa.entity.UserAssignedSystem;
import com.cyberintech.vrisk.server.model.jpa.entity.UserAssignedVendor;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;

/**
 * Email send Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2023-12-25
 */
public interface EmailService {

	/**
	 * Send user registration email
	 *
	 * @return Answer Weights List
	 */
	void sendUserRegistrationEmail(Users user);

	/**
	 * Send password reset email
	 *
	 * @return Answer Weights List
	 */
	void sendResetPasswordEmail(Users user);

	/**
	 * Send email notification for User when new System Assigned
	 *
	 * @param userAssignedItem
	 */
	void sendUserAssignment(UserAssignedSystem userAssignedItem);

	/**
	 * Send email notification for User when new Vendor Assigned
	 *
	 * @param userAssignedItem
	 */
	void sendUserAssignment(UserAssignedVendor userAssignedItem);

}
