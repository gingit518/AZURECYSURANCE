package com.cyberintech.vrisk.server.integration.bigid.batch.helper;

import com.cyberintech.vrisk.server.integration.bigid.batch.helper.audit.vo.CreateAuditRecordEvent;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.audit.vo.UpdateAuditRecordEvent;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.RoleType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.jpa.RoleRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserImportHelper {

	private final ApplicationEventPublisher applicationEventPublisher;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;

	public Users mapToUser(Organizations currentOrganization, Users currentUser, String email, String firstLastName,
						   String phone, List<RoleType> roles) {
		log.info("Processing user data: email = {}, first name = {}, phone = {}, organization = {}, user importer = {}.",
			email, firstLastName, phone, new OrganizationRefDTO(currentOrganization), new UserRefDTO(currentUser));
		if (StringUtils.isBlank(email)) {
			log.warn("User email is blank. User will be skipped.");
			return null;
		}
		Users user = userRepository.findByEmail(email).orElseGet(Users::new);

		user.setEmail(email);
		Optional.ofNullable(StringUtils.substringBeforeLast(firstLastName, " ")).map(StringUtils::trimToEmpty)
			.filter(StringUtils::isNoneBlank).ifPresent(user::setFirstName);
		Optional.ofNullable(StringUtils.substringAfterLast(firstLastName, " ")).map(StringUtils::trimToEmpty)
			.filter(StringUtils::isNoneBlank).ifPresent(user::setLastName);
		Optional.ofNullable(phone).map(StringUtils::trimToEmpty).filter(StringUtils::isNoneBlank).ifPresent(user::setMobilePhone);
		if (StringUtils.isAllBlank(user.getFirstName(), user.getLastName())) {
			user.setFirstName(email);
		}

		for (RoleType role : Optional.ofNullable(roles).orElse(Collections.emptyList())) {
			user.getRoles().add(roleRepository.findOneByName(role.role()));
		}

		user.setUpdatedAt(new Date());
		user.setUpdatedBy(currentUser);
		log.info("User was mapped.");
		return user;
	}

	public Users updateUser(Organizations currentOrganization, Users user) {
		Users saved = userRepository.save(user);
		applicationEventPublisher.publishEvent(new UpdateAuditRecordEvent(this.getClass(),
			VItemType.USER,
			saved.getId(),
			new UserRefDTO(saved),
			Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, currentOrganization.getId()),
				AuditLogItemId.of(VItemType.BIG_ID_USER_SYNC, saved.getId())).toArray(AuditLogItemId[]::new),
			null
		));
		return saved;
	}

	public Users createUser(Organizations currentOrganization, Users currentUser,
							Users user) {
		user.setOrganization(currentOrganization);
		user.setCreatedAt(user.getUpdatedAt());
		user.setCreatedBy(currentUser);
		Users saved = userRepository.save(user);
		applicationEventPublisher.publishEvent(new CreateAuditRecordEvent(this.getClass(), VItemType.USER, saved.getId(),
			new UserRefDTO(saved),
			Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, currentOrganization.getId()),
				AuditLogItemId.of(VItemType.BIG_ID_USER_SYNC, saved.getId())).toArray(AuditLogItemId[]::new)));
		return saved;
	}
}
