package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FeedsFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.user_messages.UserMessageDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.MessageStatus;
import com.cyberintech.vrisk.server.model.jpa.entity.UserMessages;
import com.cyberintech.vrisk.server.repository.jpa.UserMessagesRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.*;

/**
 * User Messages management Service. Implements basic CRUD.
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2023-01-11
 */
@Service
public class UserMessageService {

	@Autowired
	UserMessagesRepository userMessagesRepository;

	@Autowired
	UserService userService;

	@Autowired
	DocumentService documentService;

	@Autowired
	private UserRepository userRepository;


	/**
	 * Get User Message Item Id
	 *
	 * @return User Message Item Details
	 */
	public UserMessageDTO getDetails(Long itemId) {

		UserMessages itemDetails;

		try {
			itemDetails = userMessagesRepository.findById(itemId).get();

		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Message not found in the database [{0}]", itemId));
		}

		return new UserMessageDTO(itemDetails);
	}

	/**
	 * Get User Message Item
	 *
	 * @param itemId
	 * @return User Message Details
	 */
	public UserMessages getItem(Long itemId) {
		UserMessages itemDetails;

		try {
			itemDetails = userMessagesRepository.findById(itemId).get();

		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("User Message not found in the database [{0}]", itemId));
		}

		return itemDetails;
	}

	/**
	 * Create New User Message Item
	 *
	 * @return New User Message Item
	 */
	public UserMessageDTO create(UserMessageDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		UserMessages newItem = new UserMessages();
		newItem.setFromUserId(userService.getCurrentUser().getUserId());
		newItem.setToUserId(newItemDTO.getMessageTo().getId());
		newItem.setCreatedAt(new Date());
		newItem.setUpdatedAt(new Date());
		newItem.setSubject(newItemDTO.getSubject());
		newItem.setBody(newItemDTO.getBody());
		newItem.setStatus(MessageStatus.Sent);
		Optional.ofNullable(newItemDTO.getDocuments()).ifPresent(documentDTOList -> {
			newItem.setDocuments(new HashSet<>());
			documentDTOList.stream().forEach(documentDTO -> {
				newItem.getDocuments().add(documentService.getItem(documentDTO.getId()));
			});
		});

		UserMessages saveResult = userMessagesRepository.save(newItem);

		return getDetails(saveResult.getId());
	}

	/**
	 * Send messages between users
	 *
	 * @param userToId
	 * @param subject
	 * @param body
	 * @return
	 */
	public Long sendMessage(Long userToId, String subject, String body) {
		return sendMessage(null, userToId, subject, body, null);
	}

	/**
	 * Send messages between users
	 *
	 * @param userFromId
	 * @param userToId
	 * @param subject
	 * @param body
	 * @param documents
	 * @return
	 */
	public Long sendMessage(Long userFromId, Long userToId, String subject, String body, List<DocumentDTO> documents) {

		if (userFromId == null && userService.isAuthorized()) userFromId = userService.getCurrentUser().getUserId();

		// Creating message items
		UserMessages newItem = new UserMessages();
		newItem.setFromUserId(userFromId);
		newItem.setToUserId(userToId);
		newItem.setCreatedAt(new Date());
		newItem.setUpdatedAt(new Date());
		newItem.setSubject(subject);
		newItem.setBody(body);
		newItem.setStatus(MessageStatus.Sent);

		// Attach documents if present
		if (CollectionUtils.isNotEmpty(documents)) {
			newItem.setDocuments(new HashSet<>());
			documents.stream().forEach(documentDTO -> {
				newItem.getDocuments().add(documentService.getItem(documentDTO.getId()));
			});
		}

		UserMessages saveResult = userMessagesRepository.save(newItem);

		return saveResult.getId();
	}

	/**
	 * Update User Message Item
	 *
	 * @return Received User Message
	 */
	public UserMessageDTO receiveMessage(Long itemId) {

		UserMessages receivedMessage = getItem(itemId);

		if (receivedMessage.getStatus() == MessageStatus.Sent) {
			receivedMessage.setStatus(MessageStatus.Received);
			receivedMessage.setUpdatedAt(new Date());
			userMessagesRepository.save(receivedMessage);
		}
		return new UserMessageDTO(receivedMessage);

	}


	/**
	 * Update User Message Item
	 *
	 * @return Read User Message
	 */
	public UserMessageDTO readMessage(Long itemId) {

		UserMessages receivedMessage = getItem(itemId);

		if (receivedMessage.getStatus() != MessageStatus.Read) {
			receivedMessage.setStatus(MessageStatus.Read);
			receivedMessage.setUpdatedAt(new Date());
			userMessagesRepository.save(receivedMessage);
		}

		return new UserMessageDTO(receivedMessage);

	}

	/**
	 * Deletes User Message
	 *
	 * @return ID of removed message
	 */
	@Transactional
	public Long delete(Long itemId) {

		UserMessages existingItem = getItem(itemId);
		userMessagesRepository.delete(existingItem);
		userMessagesRepository.flush();

		return itemId;
	}

	/**
	 * Obtain messages for current user
	 *
	 * @param filteredRequest
	 * @return
	 */
	public FilteredResponse<FeedsFilter, UserMessageDTO> getListFiltered(FilteredRequest<FeedsFilter> filteredRequest) {
		List<UserMessageDTO> items = null;
		Long count = 0l;

		// List<UserMessages> unreadMessages = userMessagesRepository.getListUnreadByUserId(userService.getCurrentUserEntity().getId(), filteredRequest.toPageRequest());
		List<UserMessages> unreadMessages = userMessagesRepository.getListUnfilteredByUserId(userService.getCurrentUserEntity().getId(), filteredRequest.toPageRequest());
		items = DTOBase.fromEntitiesList(unreadMessages, UserMessageDTO.class);
		count = userMessagesRepository.getCountUnfilteredByUserId(userService.getCurrentUserEntity().getId());

		// items.forEach(message -> receiveMessage(message.getId()));
		items.forEach(message -> enhanceMessageWithUserDetails(message));

		FilteredResponse<FeedsFilter, UserMessageDTO> filteredResponse = new FilteredResponse<>(filteredRequest);
		filteredResponse.setItems(items);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Enhance message details
	 *
	 * @param message
	 */
	public void enhanceMessageWithUserDetails(UserMessageDTO message) {
		if (message.getMessageTo() != null) documentService.buildStorageUrl(message.getMessageTo().getPhoto());
		if (message.getMessageFrom() != null) documentService.buildStorageUrl(message.getMessageFrom().getPhoto());
	}


}
