package com.cyberintech.vrisk.server.model.jpa.entity.workflows;

import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.model.jpa.entity.converters.MapOfObjectsConverter;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Workflow Execution Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  2.0.1
 * @since 	 2022-11-07
 */
@Entity
@Table(name = "workflow_execution")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class WorkflowExecution {

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
	@JoinColumn(name = "user_id")
	private Users user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "process_id")
	private WorkflowProcess process;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "root_parent_id")
	private WorkflowExecution rootParent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private WorkflowExecution parent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_id")
	private WorkflowTask task;

	@SuppressWarnings("JpaAttributeTypeInspection")
	@Column(name = "data")
	@Convert(converter = MapOfObjectsConverter.class)
	private Map<String, Object> data;

	@Column(name = "status")
	private Long status;

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name = "execution_id")
	private Set<WorkflowTaskInstance> taskInstances = new HashSet<>();

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "started_at")
	private Date startedAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "finished_at")
	private Date finishedAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "expiration_date")
	private Date expirationDate;

}
