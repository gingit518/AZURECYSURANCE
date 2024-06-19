package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Andrii Iakovenko
 * @since  2022-08-08
 */
@Entity
@Table(name = "oauth_access_token")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = { "id", "userName" })
@EqualsAndHashCode(of = { "id", "token" })
public class AccessToken {

	@Id
	@Column(name = "token_id")
	private String id;

	@Column(name = "token")
	private byte[] token;

	@Column(name = "user_name")
	private String userName;

}
