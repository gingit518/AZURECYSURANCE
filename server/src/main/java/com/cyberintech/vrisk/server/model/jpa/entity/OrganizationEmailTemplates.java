package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationEmailTemplateType;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Organization Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-08
 */
@Entity
@Table(name = "organization_email_templates")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "organizationId", "type"})
@EqualsAndHashCode(of = {"id"})
public class OrganizationEmailTemplates {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id", nullable = false)
	private Long organizationId;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private OrganizationEmailTemplateType type;

	@Column(name = "subject")
	private String subject;

	@Column(name = "content")
	private String content;

}
