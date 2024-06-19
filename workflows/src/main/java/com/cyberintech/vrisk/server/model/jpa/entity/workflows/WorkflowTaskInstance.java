package com.cyberintech.vrisk.server.model.jpa.entity.workflows;

import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.model.jpa.entity.converters.MapOfObjectsConverter;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Workflow Task Instance Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  2.0.1
 * @since 	 2022-11-07
 */
@Entity
@Table(name = "workflow_task_instance")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class WorkflowTaskInstance {

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
	@JoinColumn(name = "task_type_id")
	private WorkflowTaskType taskType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "root_execution_id")
	private WorkflowExecution rootExecution;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "execution_id")
	private WorkflowExecution execution;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_id")
	private WorkflowTask task;

	@SuppressWarnings("JpaAttributeTypeInspection")
	@Column(name = "data")
	@Convert(converter = MapOfObjectsConverter.class)
	private Map<String, Object> data;

	@Column(name = "status")
	private Long status;

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
