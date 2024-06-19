package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.auth.UserDetailsImpl;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.risk_domains.RiskDomainViewDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserCreateDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserListDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserUpdateDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskDomains;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.jpa.RiskDomainRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import com.cyberintech.vrisk.server.rest.exception.NotAuthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Risk Domains management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-10-27
 */
@Service
public class RiskDomainService {

	@Autowired
	private RiskDomainRepository riskDomainRepository;

	/**
	 * Get Risk Domains List
	 *
	 * @return Risk Domains List
	 */
	public List<RiskDomainViewDTO> getList() {
		List<RiskDomains> items = riskDomainRepository.findAll();

		List<RiskDomainViewDTO> itemDTOs = RiskDomainViewDTO.fromEntitiesList(items, RiskDomainViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Risk Domain details
	 *
	 * @return Risk Domain Details
	 */
	public RiskDomainViewDTO getDetails(Long itemId) {

		RiskDomains itemDetails = getRiskDomain(itemId);
		RiskDomainViewDTO itemDTO = new RiskDomainViewDTO(itemDetails);

		return itemDTO;
	}

	/**
	 * Get Risk Domain details
	 *
	 * @return Risk Domain Details
	 */
	public RiskDomains getRiskDomain(Long itemId) {
		RiskDomains itemDetails;

		try {
			itemDetails = riskDomainRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Risk Domain not found in the database [{0}]", itemId));
		}

		return itemDetails;
	}

}
