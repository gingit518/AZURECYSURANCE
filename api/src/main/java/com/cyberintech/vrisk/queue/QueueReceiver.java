package com.cyberintech.vrisk.queue;

import com.cyberintech.vrisk.server.model.data.queue.MessageType;
import com.cyberintech.vrisk.server.model.data.queue.RiskQQueueMessage;
import com.cyberintech.vrisk.server.service.OrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class QueueReceiver {

	@Autowired
	private OrganizationService organizationService;

	// @JmsListener(destination = "riskq-development-ekalosha", containerFactory = "jmsListenerContainerFactory")
	public void receiveMessageString(String message) {
		log.info("Received message from queue: {}", message);
	}

	@JmsListener(destination = "${application.queue.common:riskq-development}", containerFactory = "jmsListenerContainerFactory")
	public void receiveMessage(RiskQQueueMessage message) {

		log.info("Received message from queue: {}, {}, {}", message.getType(), message.getUserId(), message.getCreateDate());
		if (MessageType.SIGNIN.equalsIgnoreCase(message.getType())) {
			try {
				organizationService.updateOrganizationPowerBICapacity(message.getUserId());
			} catch (Exception exception) {
				log.warn("Failed to update Organization PowerBI Capacity State: " + exception.getMessage());
			}
		} else if (MessageType.SIGNOUT.equalsIgnoreCase(message.getType())) {
			try {
				organizationService.releaseOrganizationPowerBICapacity(message.getUserId());
			} catch (Exception exception) {
				log.warn("Failed to release Organization PowerBI Capacity State: " + exception.getMessage());
			}
		}

	}

}
