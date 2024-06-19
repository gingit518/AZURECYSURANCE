package com.cyberintech.vrisk.server.security.cache;

import com.cyberintech.vrisk.server.model.config.CacheNode;
import com.cyberintech.vrisk.server.security.mfa.MFACodeTokenDTO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Cache Storage Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-01
 */
@Service
public class IdPCacheService {

	@Cacheable(cacheNames = CacheNode.OTP_CODES, key = "#key")
	public MFACodeTokenDTO getCodeToken(String key) {
		return null;
	}

	@CachePut(cacheNames = CacheNode.OTP_CODES, key = "#value.token")
	public MFACodeTokenDTO putCodeToken(MFACodeTokenDTO value) {
		return value;
	}

	@CacheEvict(cacheNames = CacheNode.OTP_CODES, key = "#key")
	public void removeCodeToken(String key) {
	}

}
