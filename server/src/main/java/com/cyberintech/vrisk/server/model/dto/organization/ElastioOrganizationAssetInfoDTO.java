package com.cyberintech.vrisk.server.model.dto.organization;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.domains.elastio.PlatformAssetType;
import com.cyberintech.vrisk.server.model.jpa.domains.elastio.PlatformType;
import com.cyberintech.vrisk.server.model.jpa.entity.OrganizationAssetInfo;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;

/**
 * Organization View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-02-22
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "platformType"})
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElastioOrganizationAssetInfoDTO extends DTOBase<OrganizationAssetInfo> {

	private Long id;
	private String uid;

	private PlatformType platformType;
	private PlatformAssetType assetType;
	private Double amountOfDataInTerabytes;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public ElastioOrganizationAssetInfoDTO(OrganizationAssetInfo entity) {
		super(entity);
	}

	/**
	 * Converts from entity to DTO
	 *
	 * @param entity
	 */
	@Override
	public void fromEntity(OrganizationAssetInfo entity) {
//		super.fromEntity(entity);

		id = entity.getId();

		uid = entity.getUid();
		platformType = entity.getPlatformType();
		assetType = entity.getAssetType();
		amountOfDataInTerabytes = entity.getAmountOfDataInTerabytes();
	}
}
