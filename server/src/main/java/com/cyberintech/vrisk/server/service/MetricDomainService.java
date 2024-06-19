package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dto.qual_metrics.MetricDomainViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.MetricDomains;
import com.cyberintech.vrisk.server.repository.jpa.MetricDomainRepository;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Metric Domains management Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-10
 */
@Service
public class MetricDomainService {

	@Autowired
	private MetricDomainRepository metricDomainRepository;

	/**
	 * Get Metric Domains List
	 *
	 * @return Metric Domains List
	 */
	public List<MetricDomainViewDTO> getList() {

		List<MetricDomains> items = metricDomainRepository.findAll();

		List<MetricDomainViewDTO> itemDTOs = MetricDomainViewDTO.fromEntitiesList(items, MetricDomainViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Metric Domains List
	 *
	 * @return Metric Domains List
	 */
	public List<MetricDomainViewDTO> getListByTypeAndNotEmpty(VendorType vendorType, Long riskModelId, String categoryCode) {

		List<MetricDomains> items = StringUtils.isEmpty(categoryCode) ? metricDomainRepository.getAllByTypeAndNotEmpty(vendorType, riskModelId)
			: metricDomainRepository.getAllByTypeAndNotEmptyAndCategoryCode(vendorType, riskModelId, categoryCode);

		List<MetricDomainViewDTO> itemDTOs = MetricDomainViewDTO.fromEntitiesList(items, MetricDomainViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Metric Domains codes List
	 *
	 * @return Metric Domains List
	 */
	@Transactional
	public List<String> getAllCodesByTypeAndNotEmpty(VendorType vendorType, Long riskModelId) {

		Set<String> codesSet = metricDomainRepository.getAllCodesByTypeAndNotEmpty(vendorType, riskModelId);

		List<String> result = (CollectionUtils.isNotEmpty(codesSet)) ? new ArrayList<>(codesSet).stream().filter(s -> s != null && StringUtils.isNotEmpty(s.trim())).toList() : new ArrayList<>();

		return result;
	}

	/**
	 * Get Metric Domain details
	 *
	 * @return Metric Domain Details
	 */
	public MetricDomains getQuestionWeight(Long itemId) {
		MetricDomains itemDetails;

		try {
			itemDetails = metricDomainRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Metric Domain not found in the database [{0}]", itemId));
		}

		return itemDetails;
	}


}
