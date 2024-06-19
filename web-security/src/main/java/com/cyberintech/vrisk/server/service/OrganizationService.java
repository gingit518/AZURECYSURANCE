package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.context.ApplicationContextThreadLocal;
import com.cyberintech.vrisk.server.model.auth.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Organization management Service. Implements basic Organization logic.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-08
 */
@Service("organizationService")
public class OrganizationService {

	@Autowired
	private UserService userService;

	/**
	 * Get Current Organization Id
	 *
	 * @return
	 */
	public Long getCurrentOrganizationId() {
		UserDetailsImpl user = userService.getCurrentUser();

		Long organizationId = user.getOrganizationId();

		if (organizationId == null && userService.isSuperAdmin()) {
			organizationId = ApplicationContextThreadLocal.getContext().getOrganizationId();
		}

		return organizationId;
	}

}
