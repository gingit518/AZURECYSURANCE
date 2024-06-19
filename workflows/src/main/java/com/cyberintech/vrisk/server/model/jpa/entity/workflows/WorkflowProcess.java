package com.cyberintech.vrisk.server.model.jpa.entity.workflows;

import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.converters.MapOfObjectsConverter;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Workflow Process Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  2.0.1
 * @since 	 2022-11-07
 */
@Entity
@Table(name = "workflow_process")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class WorkflowProcess {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id", insertable = false, updatable = false)
	private Organizations organization;

	@Column(name = "source_id")
	private Long sourceId;

	@Column(name = "deployment_uid", length = 64)
	private String deploymentUid;

	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "description")
	private String description;

	@SuppressWarnings("JpaAttributeTypeInspection")
	@Column(name = "data")
	@Convert(converter = MapOfObjectsConverter.class)
	private Map<String, Object> data;

	@Column(name = "version")
	private Long version;

	@Column(name = "parent_id")
	private Long parentId;

	@Column(name = "status")
	private Long status;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

}
