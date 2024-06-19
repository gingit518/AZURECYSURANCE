package com.cyberintech.vrisk.server.repository.jpa;
import com.cyberintech.vrisk.server.model.jpa.entity.UserMessages;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserMessagesRepository extends CoreRepository<UserMessages, Long> {
	Optional<UserMessages> findById(Long id);

	@Query("SELECT msg FROM UserMessages msg LEFT JOIN FETCH msg.messageTo msgto WHERE  msgto.id = :userId AND msg.status = 'Sent'")
	List<UserMessages> getListSentByUserId(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT count(msg) FROM UserMessages msg WHERE msg.messageTo.id = :userId AND msg.status = 'Sent'")
	Long getCountSentByUserId(@Param("userId") Long userId);

	@Query("SELECT msg FROM UserMessages msg LEFT JOIN FETCH msg.messageTo msgto WHERE  msgto.id = :userId")
	List<UserMessages> getListUnfilteredByUserId(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT msg FROM UserMessages msg LEFT JOIN FETCH msg.messageTo msgto WHERE  msgto.id = :userId AND (msg.status = 'Sent' OR msg.status = 'Received')")
	List<UserMessages> getListUnreadByUserId(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT count(msg) FROM UserMessages msg WHERE msg.messageTo.id = :userId")
	Long getCountUnfilteredByUserId(@Param("userId") Long userId);

	@Query("SELECT count(msg) FROM UserMessages msg WHERE msg.messageTo.id = :userId AND (msg.status = 'Sent' OR msg.status = 'Received')")
	Long getCountUnreadByUserId(@Param("userId") Long userId);

}
