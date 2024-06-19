package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.LanguageConstantScopeType;
import lombok.*;

import javax.persistence.*;

/**
 * Language Constant Entity Definition
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-04-08
 */
@Entity
@Table(name = "language_constants")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"})
public class LanguageConstants {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "name", length = 127, nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "scope")
	private LanguageConstantScopeType scope;

}
