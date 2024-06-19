package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dto.qual_metrics.MetricDomainViewDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.QualMetricsEditDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.QualMetricsViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.RoleType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.MetricDomainRepository;
import com.cyberintech.vrisk.server.repository.jpa.QualMetricsRepository;
import com.cyberintech.vrisk.server.repository.jpa.QualitativeQuestionRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Qual Metrics management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@Service
public class QualMetricsService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private MetricDomainRepository metricDomainRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private QualitativeQuestionRepository qualitativeQuestionRepository;

	@Autowired
	private QualMetricsRepository qualMetricsRepository;

	@Autowired
	private RiskModelService riskModelService;

	@Autowired
	private UserService userService;

	/**
	 * Get Qual Metrics List
	 *
	 * @return Qual Metrics List
	 */
	public List<QualMetricsViewDTO> getList() {
		List<QualMetrics> items = qualMetricsRepository.findAll();

		List<QualMetricsViewDTO> itemDTOs = QualMetricsViewDTO.fromEntitiesList(items, QualMetricsViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Qual Metric details
	 *
	 * @return Qual Metric Details
	 */
	public QualMetricsViewDTO getDetails(Long itemId) {

		QualMetrics itemDetails = getQualMetricForOrganization(itemId);

		QualMetricsViewDTO itemDTO = new QualMetricsViewDTO(itemDetails);

		return itemDTO;
	}

	/**
	 * Get Qual Metric details
	 *
	 * @return Qual Metric Details
	 */
	public QualMetrics getQualMetric(Long itemId) {
		QualMetrics itemDetails;

		try {
			itemDetails = qualMetricsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Qual Metric not found in the database [{0}]", itemId));
		}

		// Verify Risk Model and Organization
		RiskModels riskModel = riskModelService.getRiskModel(itemDetails.getRiskModelId());

		return itemDetails;
	}

	/**
	 * Get Qual Metric details
	 *
	 * @return Qual Metric Details
	 */
	public QualMetrics getQualMetricForOrganization(Long itemId) {
		QualMetrics itemDetails;

		try {
			itemDetails = qualMetricsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Qual Metric not found in the database [{0}]", itemId));
		}

		// Verify Risk Model and Organization
		RiskModels riskModel = riskModelService.getRiskModel(itemDetails.getRiskModelId());

		// Verify Process and Organization
		if (!organizationService.getCurrentOrganizationId().equals(riskModel.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Qual Metric [{0}] doesn't match your organization [{1}]", riskModel.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Risk Qual Metrics List inside current Organization
	 *
	 * @return Qual Metrics List
	 */
	public List<QualMetricsViewDTO> getListByRiskModel(Long riskModelId) {

		RiskModels riskModel = riskModelService.getRiskModel(riskModelId);

		List<QualMetrics> items = qualMetricsRepository.getListByRiskModelId(riskModelId);

		List<QualMetricsViewDTO> itemDTOs = QualMetricsViewDTO.fromEntitiesList(items, QualMetricsViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get or create item by Risk Model and Qual Domain
	 *
	 * @param riskModelId
	 * @param qualMetricDomain
	 * @return
	 */
	public QualMetricsViewDTO getOrCreateOneByRiskModelAndDomain(Long riskModelId, Long organizationId, String qualMetricDomain, String domainCategory) {

		QualMetricsViewDTO result = null;

		if (StringUtils.isNotEmpty(qualMetricDomain)) {
			List<QualMetrics> items = qualMetricsRepository.getOneByRiskModelAndMetricDomain(riskModelId, qualMetricDomain);
			if (items.isEmpty()) {
				Optional<MetricDomains> metricDomain = metricDomainRepository.findFirstByNameIgnoreCase(qualMetricDomain);
				if (metricDomain.isEmpty()) metricDomain = metricDomainRepository.findFirstByCodeIgnoreCase(qualMetricDomain);

				MetricDomains metricDomains = null;
				if (metricDomain.isEmpty() && (userService.isSuperAdmin()) || userService.hasRole(RoleType.ORGANIZATION_ADMIN)) {
					metricDomains = new MetricDomains();
					metricDomains.setName(qualMetricDomain);
					metricDomains.setCode(qualMetricDomain.toUpperCase().replaceAll("\\s+", "_"));
					if (StringUtils.isNotEmpty(domainCategory)) {
						metricDomains.setCategoryCode(domainCategory);
					}
					metricDomains.setOrganizationId(organizationId);
					metricDomains = metricDomainRepository.saveAndRefresh(metricDomains);
				} else if (metricDomain.isPresent()) {
					metricDomains = metricDomain.get();
				}

				if (metricDomains != null) {
					QualMetricsEditDTO newItem = new QualMetricsEditDTO();
					newItem.setMetricDomain(new MetricDomainViewDTO(metricDomains));
					newItem.setName(metricDomains.getName());
					newItem.setDescription(metricDomains.getDescription());
					newItem.setRiskModelId(riskModelId);
					result = create(newItem);
				}
			} else {
				result = new QualMetricsViewDTO(items.get(0));
			}
		}

		return result;
	}

	/**
	 * Create new Qual Metric
	 *
	 * @return New Qual Metric
	 */
	public QualMetricsViewDTO create(QualMetricsEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		RiskModels riskModel = riskModelService.getRiskModel(newItemDTO.getRiskModelId());

		Long metricDomainId = null;
		if (newItemDTO.getMetricDomain() != null & newItemDTO.getMetricDomain().getId() != null) {
			metricDomainId = metricDomainRepository.findById(newItemDTO.getMetricDomain().getId()).get().getId();
		}

//		QualMetrics newItem = newItemDTO.toEntity();
		QualMetrics newItem = new QualMetrics();
		newItem.setName(newItemDTO.getName());
		newItem.setDescription(newItemDTO.getDescription());
		newItem.setRiskModelId(riskModel.getId());
		newItem.setOrdinal(newItemDTO.getOrdinal());
		newItem.setMetricDomainId(metricDomainId);
		newItem.setCreatedAt(new Date());
		newItem.setUpdatedAt(new Date());
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setUpdatedBy(userService.getCurrentUserEntity());
		QualMetrics saveResult = qualMetricsRepository.save(newItem);

		QualMetricsViewDTO result = new QualMetricsViewDTO(saveResult);

		// Save Audit Log CREATE event
		QualMetricsEditDTO saveResultDTO = new QualMetricsEditDTO(saveResult);
		auditLogService.create(
			VItemType.QUALITATIVE_METRIC,
			saveResult.getId(),
			saveResultDTO,
			collectAuditLogItems(saveResultDTO, riskModel.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update Risk Qual Metric
	 *
	 * @return Updated Qual Metrics
	 */
	public QualMetricsViewDTO update(QualMetricsEditDTO itemDTO) {

		QualMetricsViewDTO result;

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		QualMetrics existingItem = getQualMetricForOrganization(itemDTO.getId());
		QualMetricsEditDTO existingItemDTO = new QualMetricsEditDTO(existingItem);

		// Verify Risk Model and Organization Id
		RiskModels riskModel = riskModelService.getRiskModel(existingItem.getRiskModelId());

		Long metricDomainId = null;
		if (itemDTO.getMetricDomain() != null & itemDTO.getMetricDomain().getId() != null) {
			metricDomainId = metricDomainRepository.findById(itemDTO.getMetricDomain().getId()).get().getId();
		}

		// Update item details
		existingItem.setName(itemDTO.getName());
		existingItem.setDescription(itemDTO.getDescription());
		existingItem.setMetricDomainId(metricDomainId);
		existingItem.setUpdatedAt(new Date());
		existingItem.setUpdatedBy(userService.getCurrentUserEntity());

		// Save to the database
		QualMetrics saveResult = qualMetricsRepository.save(existingItem);

		result = new QualMetricsViewDTO(saveResult);

		// Save Audit Log UPDATE event
		QualMetricsEditDTO saveResultDTO = new QualMetricsEditDTO(saveResult);
		auditLogService.update(
			VItemType.QUALITATIVE_METRIC,
			saveResult.getId(),
			existingItemDTO,
			saveResultDTO,
			collectAuditLogItems(saveResultDTO, riskModel.getOrganizationId())
		);

		return result;
	}


	/**
	 * Deletes Qualitative metric
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		QualMetrics existingItem = getQualMetricForOrganization(itemId);
		QualMetricsEditDTO existingItemDTO = new QualMetricsEditDTO(existingItem);

		// Checking questions dependencies
		Set<QualitativeQuestions> questions = qualitativeQuestionRepository.findAllByQualitativeMetric(existingItem);
		if (CollectionUtils.isNotEmpty(questions)) {
			String questionsString = questions.stream().map(QualitativeQuestions::getQuestion).collect(Collectors.joining("\\n"));

			throw new ConflictException("Cannot delete metric as there are questions which use this metric: \\n" + questionsString);
		}

		qualMetricsRepository.delete(existingItem);
		qualMetricsRepository.flush();

		RiskModels riskModel = riskModelService.getRiskModel(existingItem.getRiskModelId());

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.QUALITATIVE_METRIC,
			existingItemDTO.getId(),
			existingItemDTO,
			collectAuditLogItems(existingItemDTO, riskModel.getOrganizationId())
		);

		return itemId;
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItems(QualMetricsEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));
		if (existingItemDTO.getMetricDomain() != null) logItems.add(AuditLogItemId.of(VItemType.QUALITATIVE_METRIC_DOMAIN, existingItemDTO.getMetricDomain().getId()));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
