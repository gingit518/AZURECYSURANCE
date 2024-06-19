package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.MessageStatus;
import lombok.*;
import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import static javax.persistence.GenerationType.IDENTITY;

/**
 * User Messages Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2023-01-11
 */
@Entity
@Table(name = "user_messages")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "messageFrom"})
@EqualsAndHashCode(of = {"id", "messageFrom"})
public class UserMessages {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "from_user")
	private Long fromUserId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "from_user", insertable = false, updatable = false)
	private Users messageFrom;

	@Column(name = "to_user")
	private Long toUserId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "to_user", insertable = false, updatable = false)
	private Users messageTo;

	@Column(name = "created_at", nullable = false)
	private Date createdAt;

	@Column(name = "updated_at", nullable = false)
	private Date updatedAt;

	@Column(name = "subject", nullable = true)
	private String subject;

	@Column(name = "body", nullable = true)
	private String body;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private MessageStatus status;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "message_attachments",
		joinColumns = {@JoinColumn(name = "message_id")},
		inverseJoinColumns = {@JoinColumn(name = "document_id")}
	)
	private Set<Documents> documents = new HashSet<>();
}
