package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.entity.common.IMetadataAware;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Technology Categories Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2024-01-21
 */
@Entity
@Table(name = "technology_category_import_mappings")
@NoArgsConstructor
@Setter
@Getter
@Builder
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
@AllArgsConstructor
public class TechnologyCategoryImportMappings {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id", insertable = false, updatable = false)
	private Organizations organization;

	@Column(name = "technology_category", nullable = false, length = 255)
	private String technologyCategory;

	@Column(name = "technology_name")
	private String technologyName;

	@Column(name = "technology_vendor")
	private String technologyVendor;

	@Column(name = "target_technology_category")
	private String targetTechnologyCategory;

	@Column(name = "target_technology_subcategory")
	private String targetTechnologySubcategory;

	@Column(name = "target_technology_class")
	private String targetTechnologyClass;

	@Column(name = "target_technology_name")
	private String targetTechnologyName;

}
