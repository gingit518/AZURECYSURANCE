package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.HintType;
import com.cyberintech.vrisk.server.model.jpa.entity.converters.MapOfObjectsConverter;
import lombok.*;

import javax.persistence.*;
import java.util.Map;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Hints Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-04-04
 */
@Entity
@Table(name = "hints")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "code", "hintType"})
@EqualsAndHashCode(of = {"id"})
public class Hints {

	/*
	id serial,
	hint_type HintType DEFAULT NULL,
	code character varying (48) NOT NULL,
	title character varying (255) NOT NULL,
	body character varying (255) NOT NULL,
	footer character varying (255) NOT NULL,
	link character varying (255) NOT NULL,
	properties jsonb DEFAULT NULL,
	 */

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "hint_type")
	private HintType hintType;

	@Column(name = "name")
	private String name;

	@Column(name = "code", unique = true, nullable = false)
	private String code;

	@Column(name = "title")
	private String title;

	@Column(name = "body")
	private String body;

	@Column(name = "footer")
	private String footer;

	@Column(name = "link")
	private String link;

	@Column(name = "external_id")
	private String externalId;

	@SuppressWarnings("JpaAttributeTypeInspection")
	@Column(name = "properties")
	@Convert(converter = MapOfObjectsConverter.class)
	private Map<String, Object> properties;

}
