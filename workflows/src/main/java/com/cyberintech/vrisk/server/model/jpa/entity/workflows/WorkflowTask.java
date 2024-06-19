package com.cyberintech.vrisk.server.model.jpa.entity.workflows;

import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.model.jpa.entity.converters.MapOfObjectsConverter;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Workflow Task Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  2.0.1
 * @since 	 2022-11-07
 */
@Entity
@Table(name = "workflow_task")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class WorkflowTask {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id", insertable = false, updatable = false)
	private Organizations organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "process_id")
	private WorkflowProcess process;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_type_id")
	private WorkflowTaskType taskType;

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

	@Column(name = "status")
	private Long status;

	@ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "workflow_task_children",
		joinColumns = {@JoinColumn(name = "parent_id")},
		inverseJoinColumns = {@JoinColumn(name = "child_id")}
	)
	private Set<WorkflowTask> children = new HashSet<>();

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

}
