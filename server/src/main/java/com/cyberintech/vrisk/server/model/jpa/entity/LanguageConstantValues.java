package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

/**
 * Language Constant Value Entity Definition
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-04-08
 */
@Entity
@Table(name = "language_constant_values")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "value"})
@EqualsAndHashCode(of = {"id"})
public class LanguageConstantValues {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "value", length = 127, nullable = false)
	private String value;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "language_id", nullable = false)
	private SupportedLanguages language;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "language_constant_id", nullable = false)
	private LanguageConstants languageConstant;

	/**
	 * LanguageConstant based constructor
	 */
	public LanguageConstantValues(LanguageConstants languageConstant, LanguageConstantValues languageConstantValue) {

		if (languageConstantValue == null) {
			this.id = null;
			this.value = "";
			this.languageConstant = languageConstant;
			this.language = null;
		} else {
			this.id = languageConstantValue.getId();
			this.value = languageConstantValue.getValue();
			this.language = languageConstantValue.getLanguage();
			this.languageConstant = languageConstant;
		}

	}
}
