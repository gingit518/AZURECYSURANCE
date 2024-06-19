package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.IdpType;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "idp_users")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "userId", "idpId", "userIdentity"})
@EqualsAndHashCode(of = {"id"})
public class IdpUsers {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "idp_id")
	private IdpType idpId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private Users user;

	@Column(name = "idp_user_identity")
	private String userIdentity;

	@org.springframework.data.annotation.Transient
	public Long getUserId() {
		return user != null ? user.getId() : null;
	}

}
