package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.VariableTypeRelation;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Variable Type Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-02-07
 */
@Entity
@Table(name = "variable_types")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
public class VariableTypes {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "code", unique = false, nullable = true)
	private String code;

	@Column(name = "name", unique = true, nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "relation")
	private VariableTypeRelation relation;

}
