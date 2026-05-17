package com.cyberintech.vrisk.server.model.dto.organization;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Organization View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-08
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationViewDTO extends DTOBase<Organizations> {

	private Long id;
	private String name;
	private String description;
	private String logo;
	private String site;
	private OrganizationType organizationType;
	private Double averageRevenue;
	private Double qualThreshold;
	private OrganizationRefDTO parent;
	private OrganizationRefDTO rootParent;
	private UserRefDTO owner;
	private Boolean isCloudVendor;
	protected Boolean isServiceVendor;
	protected Boolean isTechnologyVendor;
	protected Boolean isSystemVendor;
	private Boolean useMultiFactorAuth;
	private Double recordPriceLimit;

	private Set<OrganizationMetadataRefDTO> entityMetadata;
	private PackagePlansDTO packagePlan;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public OrganizationViewDTO(Organizations entity) {
		super(entity);
	}

	/**
	 * Converts from entity to DTO
	 *
	 * @param entity
	 */
	@Override
	public void fromEntity(Organizations entity) {
//		super.fromEntity(entity);

		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
		logo = entity.getLogo();
		site = entity.getSite();
		organizationType = entity.getOrganizationType();
		averageRevenue = entity.getAverageRevenue();
		qualThreshold = entity.getQualThreshold();
		isCloudVendor = entity.getIsCloudVendor();
		isServiceVendor = entity.getIsServiceVendor();
		isTechnologyVendor = entity.getIsTechnologyVendor();
		isSystemVendor = entity.getIsSystemVendor();
		useMultiFactorAuth = entity.getUseMultiFactorAuth();
		recordPriceLimit = entity.getRecordPriceLimit();

		if (entity.getParent() != null) setParent(new OrganizationRefDTO(entity.getParent()));
		if (entity.getRootParent() != null) setRootParent(new OrganizationRefDTO(entity.getRootParent()));
		if (entity.getOwner() != null) setOwner(new UserRefDTO(entity.getOwner()));
		if (entity.getPackagePlan() != null) setPackagePlan(new PackagePlansDTO(entity.getPackagePlan()));

		entityMetadata = Optional.ofNullable(entity.getMetadata())
			.stream()
			.flatMap(Set::stream).map(OrganizationMetadataRefDTO::new).collect(Collectors.toSet());
	}
}
