package com.cyberintech.vrisk.idp.rest.dto;

import lombok.Data;

/**
 * IdP info configuration
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2023-02-16
 */
@Data
public class IdpInfoDTO {
	private String organizationUid;
	private Boolean oktaAllowed;
	private String oktaAppLoginUrl;
}
