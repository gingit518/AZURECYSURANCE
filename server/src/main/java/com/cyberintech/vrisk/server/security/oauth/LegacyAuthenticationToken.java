package com.cyberintech.vrisk.server.security.oauth;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class LegacyAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = -4262908723900427594L;

	private String username;

	private final UserDetails authenticatedUser;

	public LegacyAuthenticationToken(String username) {
		super(null);
		this.username = username;
		this.authenticatedUser = null;
	}

	public LegacyAuthenticationToken(Collection<? extends GrantedAuthority> authorities, String username, UserDetails authenticatedUser) {
		super(authorities);
		this.username = username;
		this.authenticatedUser = authenticatedUser;
	}

	@Override
	public Object getCredentials() {
		return "";
	}

	@Override
	public Object getPrincipal() {
		return this.authenticatedUser;
	}

	public String getUsername() {
		return this.username;
	}

}
