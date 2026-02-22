package com.cyberintech.vrisk.server.security.oauth;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;

public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

	@Serial
	private static final long serialVersionUID = 2262908723900426453L;

	@Getter
	private String username;

	private final UserDetails authenticatedUser;

	@Getter
	private final Boolean isPrivateKey;

	public ApiKeyAuthenticationToken(Collection<? extends GrantedAuthority> authorities, String username, UserDetails authenticatedUser, Boolean isPrivateKey) {
		super(authorities);
		this.username = username;
		this.authenticatedUser = authenticatedUser;
		this.isPrivateKey = isPrivateKey;
	}

	@Override
	public Object getCredentials() {
		return "";
	}

	@Override
	public Object getPrincipal() {
		return this.authenticatedUser;
	}

}
