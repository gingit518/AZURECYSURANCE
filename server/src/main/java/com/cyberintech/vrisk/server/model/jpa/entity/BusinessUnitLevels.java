package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "business_unit_levels")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString(of = {"parentId", "childId", "level"})
@EqualsAndHashCode(of = {"parentId", "childId", "level"})
public class BusinessUnitLevels {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id", nullable = false, insertable = false, updatable = false)
	private BusinessUnits parent;

	@Column(name = "parent_id", nullable = false)
	private Long parentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "child_id", nullable = false, insertable = false, updatable = false)
	private BusinessUnits child;

	@Column(name = "child_id", nullable = false)
	private Long childId;

	@Column(name = "level")
	private Long level;
}
