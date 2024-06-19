package com.cyberintech.vrisk.server.model.jpa.entity.workflows;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Workflow Task Type Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  2.0.1
 * @since 	 2022-11-07
 */
@Entity
@Table(name = "workflow_task_type")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class WorkflowTaskType {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "code", nullable = false, length = 64)
	private String code;

	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "description")
	private String description;

}
