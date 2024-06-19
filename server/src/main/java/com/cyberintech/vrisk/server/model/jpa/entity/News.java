package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * News Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2023-01-12
 */
@Entity
@Table(name = "news")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"})
public class News {
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

	@Column(name = "news_url", nullable = true)
	private String url;

	@Column(name = "date_of_news", nullable = false)
	private Date date;

	@Column(name = "expired_date", nullable = false)
	private Date expiryDate;

	@Column(name = "created_at", nullable = false)
	private Date createdAt;

	@Column(name = "updated_at", nullable = false)
	private Date updatedAt;

}
