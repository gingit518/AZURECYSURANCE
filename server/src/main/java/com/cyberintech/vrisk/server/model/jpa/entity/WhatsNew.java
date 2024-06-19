package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * What's New Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2023-01-12
 */
@Entity
@Table(name = "whats_new")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"})
public class WhatsNew {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "uid", nullable = true)
	private String uid;


	@Column(name = "title", nullable = true)
	private String name;

	@Column(name = "description", nullable = true)
	private String description;

	@Column(name = "whats_new_url", nullable = true)
	private String url;

	@Column(name = "date_of_whats_new", nullable = false)
	private Date date;

	@Column(name = "expired_date", nullable = false)
	private Date expiryDate;

	@Column(name = "created_at", nullable = false)
	private Date createdAt;

	@Column(name = "updated_at", nullable = false)
	private Date updatedAt;

}
