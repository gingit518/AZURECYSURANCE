package com.cyberintech.vrisk.server.model.jpa.entity;


import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Policies Entity
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2022-03-31
 */
@Entity
@Table(name = "organization_synchronization_external_log")
@NoArgsConstructor
@Setter
@Getter
@ToString
@EqualsAndHashCode(of = {"id"})
public class OrganizationSynchronizationExternalLog {

	public static class IntegrationType {
		public static final String ZOOMINFO = "ZOOMINFO";
	}

	public static class ObjectType {
		public static final String SYSTEM = "SYSTEM";
		public static final String VENDOR = "VENDOR";
		public static final String SUBSIDIARY_ORG = "SUB_ORG";
		public static final String ORGANIZATION = "ORG";
		public static final String TECHNOLOGY = "TECH";
		public static final String TECHNOLOGY_CATEGORY = "TECH_CAT";
		public static final String BUSINESS_UNIT = "BUS_UNIT";
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@Column(name = "integration_type", length = 8)
	private String integrationType;

	@Column(name = "object_type", length = 16)
	private String objectType;

	@Column(name = "external_id", length = 32)
	private String externalId;

	@Column(name = "local_id")
	private Long localId;

}
