package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.LanguageDirection;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Supported Language Entity Definition
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-03-16
 */
@Entity
@Table(name = "supported_languages")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"})
public class SupportedLanguages {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "name", length = 127, nullable = false)
	private String name;

	@Column(name = "iso_code", length = 10, nullable = true)
	private String code;

	@Column(name = "charset", length = 32)
	private String charset;

	@Column(name = "locale", length = 8)
	private String locale;

	@Enumerated(EnumType.STRING)
	@Column(name = "direction")
	private LanguageDirection direction;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "organization_to_language",
		joinColumns = {@JoinColumn(name = "language_id")},
		inverseJoinColumns = {@JoinColumn(name = "organization_id")}
	)
	private Set<Organizations> organizations = new HashSet<>();

	@Column(name = "is_public")
	private Boolean isPublic;

}
