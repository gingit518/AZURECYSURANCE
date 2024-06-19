package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

/**
 * Description
 *
 * @author Oleh Dmytrenko <odmytrenko@dfusiontech.com>
 * @version 0.1.1
 * @since 2022-12-29
 */
@Entity
@Table(name = "regulation_ages")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "age"})
@EqualsAndHashCode(of = {"id"})
public class RegulationAges {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "regulation_id")
	private Regulations regulation;

	@Column(name = "age")
	private Long age;

	@Column(name = "comments")
	private String comments;
}
