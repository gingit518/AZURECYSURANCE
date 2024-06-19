package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.TaskLinkageType;
import com.cyberintech.vrisk.server.model.jpa.domains.TaskLinkageTypeConverter;
import com.cyberintech.vrisk.server.model.jpa.domains.TaskPriorityType;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * User Rates Entity
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-12-03
 */
@Entity
@Table(name = "tasks")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "priority", "taskNotes"})
@EqualsAndHashCode(of = {"id"})
public class Tasks {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@Enumerated(EnumType.STRING)
	@Column(name = "priority")
	private TaskPriorityType priority;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "business_unit_id")
	private BusinessUnits businessUnit;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id")
	private Projects project;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_category_id")
	private TaskCategories taskCategory;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_manager_id")
	private Users taskManager;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_assignee_id")
	private Users taskAssignee;

	@Column(name = "name")
	private String name;

	@Column(name = "task_notes")
	private String taskNotes;

	@Column(name = "status")
	private Double status;

	@Column(name = "estimated_hours")
	private Double estimatedHours;

	@Column(name = "actual_hours")
	private Double actualHours;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "estimated_start_date")
	private Date estimatedStartDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "actual_start_date")
	private Date actualStartDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "estimated_end_date")
	private Date estimatedEndDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "actual_end_date")
	private Date actualEndDate;

	@Column(name = "linkage_type")
	@Convert(converter = TaskLinkageTypeConverter.class)
	private TaskLinkageType linkageType;
}
