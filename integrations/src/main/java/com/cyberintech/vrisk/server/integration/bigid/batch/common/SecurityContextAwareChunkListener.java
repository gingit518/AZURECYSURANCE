package com.cyberintech.vrisk.server.integration.bigid.batch.common;

import com.cyberintech.vrisk.server.model.auth.UserDetailsImpl;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@RequiredArgsConstructor
@Slf4j
public class SecurityContextAwareChunkListener implements ChunkListener {

	private final UserRepository userRepository;
	private final Long userId;

	@Override
	public void beforeChunk(ChunkContext context) {
		UserDetailsImpl principal = userRepository.findById(userId).map(UserDetailsImpl::new).orElseThrow(
			() -> new ItemNotFoundException("Can not find user by {} id."));
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(principal, null)
		);
	}

	@Override
	public void afterChunk(ChunkContext context) {
		SecurityContextHolder.clearContext();
	}

	@Override
	public void afterChunkError(ChunkContext context) {
		SecurityContextHolder.clearContext();
	}
}
