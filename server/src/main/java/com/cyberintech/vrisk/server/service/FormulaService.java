package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.formulas.FormulaViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.FormulaItems;
import com.cyberintech.vrisk.server.model.jpa.entity.Formulas;
import com.cyberintech.vrisk.server.repository.jpa.FormulaItemsRepository;
import com.cyberintech.vrisk.server.repository.jpa.FormulasRepository;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FormulaService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private FormulasRepository formulasRepository;

	@Autowired
	private FormulaItemsRepository formulaItemsRepository;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Formulas List
	 *
	 * @return Formulas List
	 */
	public List<FormulaViewDTO> getList() {
		List<Formulas> items = formulasRepository.findAll();

		List<FormulaViewDTO> itemsDTOs = DTOBase.fromEntitiesList(items, FormulaViewDTO.class);

		return itemsDTOs;
	}

	/**
	 * Get Formulas List
	 *
	 * @return Formulas List
	 */
	public FilteredResponse<NameFilter, FormulaViewDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<Formulas> items = null;
		Long count = 0L;
		FilteredResponse<NameFilter, FormulaViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = formulasRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
		count = formulasRepository.getCountByOrganizationAndName(organizationId, namePattern);

		List<FormulaViewDTO> itemsDTOs = DTOBase.fromEntitiesList(items, FormulaViewDTO.class);

		filteredResponse.setItems(itemsDTOs);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Formula Item details
	 *
	 * @return Formula Item Details
	 */
	public Formulas getFormulaForCurrentOrganization(Long itemId) {
		Formulas itemDetails;

		try {
			itemDetails = formulasRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Formula Item not found in the database [{0}]", itemId), ApplicationExceptionCodes.FORMULA_NOT_EXIST);
		}

		// Verify Formula Item and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Formula Item [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Formula Item details
	 *
	 * @return Formula Item Details
	 */
	public FormulaViewDTO getDetails(Long itemId) {
		Formulas itemDetails = getFormulaForCurrentOrganization(itemId);

		FormulaViewDTO itemDTO = new FormulaViewDTO(itemDetails);

		return itemDTO;
	}

	/**
	 * Create new Formula Item
	 *
	 * @return New Formula Item
	 */
	public FormulaViewDTO create(FormulaViewDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		Long organizationId = organizationService.getCurrentOrganizationId();
//		Formulas newItem = newItemDTO.toEntity();
		Formulas newItem = new Formulas();

		newItem.setOrganizationId(organizationId);
		newItem.setName(newItemDTO.getName());
		newItem.setDescription(newItemDTO.getDescription());
		applyEntityChanges(newItemDTO, newItem);
		Formulas saveResult = formulasRepository.save(newItem);

		FormulaViewDTO result = getDetails(saveResult.getId());

		auditLogService.create(
			VItemType.FORMULA,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, organizationId)
		);

		return result;
	}

	/**
	 * Update Formula Item
	 *
	 * @return Updated Formula Item
	 */
	public FormulaViewDTO update(FormulaViewDTO itemDTO) {
		// Get Existing item from the database
		Formulas existingItem = getFormulaForCurrentOrganization(itemDTO.getId());
		FormulaViewDTO existingItemDTO = new FormulaViewDTO(existingItem);

		// Verify Item and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Formula Item [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		Formulas saveResult = formulasRepository.save(existingItem);

		FormulaViewDTO result = getDetails(saveResult.getId());

		auditLogService.update(
			VItemType.FORMULA,
			saveResult.getId(),
			existingItemDTO,
			result,
			collectAuditLogItems(result, organizationService.getCurrentOrganizationId())
		);

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(FormulaViewDTO itemDTO, Formulas entity) {
		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());

		Optional.ofNullable(itemDTO.getFormulaItems()).ifPresent(formulaItemViewDTOList -> {
			entity.setFormulaItems(new HashSet<>());
			formulaItemViewDTOList.stream().forEach(formulaItemViewDTO -> {
				if (formulaItemViewDTO.getId() != null) {
					FormulaItems existingItem = formulaItemsRepository.findById(formulaItemViewDTO.getId()).get();
					existingItem.setName(formulaItemViewDTO.getName());
					existingItem.setDescription(formulaItemViewDTO.getDescription());
					existingItem.setOrdinal(formulaItemViewDTO.getOrdinal());
					existingItem.setValue(formulaItemViewDTO.getValue());
					existingItem.setIsOperation(formulaItemViewDTO.getIsOperation());
					existingItem.setOperation(formulaItemViewDTO.getOperation());
					if (formulaItemViewDTO.getVariableType() != null) existingItem.setVariableTypeId(formulaItemViewDTO.getVariableType().getId());
//					formulaItemViewDTO.toEntity(existingItem);
					entity.getFormulaItems().add(existingItem);
				} else {
					FormulaItems formulaItem = new FormulaItems();
					formulaItem.setName(formulaItemViewDTO.getName());
					formulaItem.setDescription(formulaItemViewDTO.getDescription());
					formulaItem.setOrdinal(formulaItemViewDTO.getOrdinal());
					formulaItem.setValue(formulaItemViewDTO.getValue());
					formulaItem.setIsOperation(formulaItemViewDTO.getIsOperation());
					formulaItem.setOperation(formulaItemViewDTO.getOperation());
					if (formulaItemViewDTO.getVariableType() != null) formulaItem.setVariableTypeId(formulaItemViewDTO.getVariableType().getId());
					FormulaItems savedItem = formulaItemsRepository.save(formulaItem);
					entity.getFormulaItems().add(savedItem);
				}
			});
		});

		String formula = buildFormula(entity);
		entity.setFormula(formula);
	}

	/**
	 * Deletes Formula Item
	 *
	 * @param itemId
	 * @return Id of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		Formulas existingItem = getFormulaForCurrentOrganization(itemId);
		formulasRepository.delete(existingItem);
		formulasRepository.flush();

		return itemId;
	}

	private AuditLogItemId[] collectAuditLogItems(FormulaViewDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

	/**
	 * Build Formula string
	 *
	 * @param entity		Formula which will be represented as String
	 * @return 				String representation of <code>entity</code>
	 */
	public String buildFormula(Formulas entity) {
		String result = "";

		if (entity != null) {
			int i = 1;
			List<FormulaItems> formulaItems = entity.getFormulaItems().stream().sorted(Comparator.comparingInt(formulaItem -> formulaItem.getOrdinal().intValue())).collect(Collectors.toList());

			for (FormulaItems formulaItem : formulaItems) {
				if (formulaItem.getIsOperation()) {
					switch (formulaItem.getOperation()) {
						case PLUS:
							result += " + ";
							break;
						case MULTIPLY:
							result += " * ";
							break;
						case MINUS:
							result += " - ";
							break;
						case DIVIDE:
							result += " / ";
							break;
						case OPEN_BRACKET:
							result += " ( ";
							break;
						case CLOSE_BRACKET:
							result += " ) ";
							break;
						case MAX:
							result += " MAX ";
							break;
						case MIN:
							result += " MIN ";
							break;
						case COMMA:
							result += ",";
							break;
						case ABS:
							result += " ABS ";
							break;
						case MEDIAN:
							result += " MEDIAN ";
							break;
						case AVERAGE:
							result += " AVERAGE ";
							break;
						case MODE:
							result += " MODE ";
							break;
						case SQRT:
							result += " SQRT ";
							break;
						case SUM:
							result += " SUM ";
							break;
						case RAND:
							result += " RAND ";
							break;
						case POWER:
							result += " POW ";
							break;
						case EXPONENT:
							result += " EXP ";
							break;
						case LOG:
							result += " LN ";
							break;
						case HYPERBOLA:
							result += " 1 / ";
							break;
					}
				} else {
					String currentValue = "";
					/*
						Possible values of variable type by relation:
							QUANT_METRIC:
								(1, CONSTANT),
								(2, PROCESS_REVENUE),
								(3, SYSTEM_NUMBER_OF_REC),
								(4, ORGANIZATION_REVENUE);
								(13, MARKET_CAPITALIZATION);
							RISK_METRIC:
								(5, LIKELIHOOD),
								(6, IMPACT),
								(7, AMPLIFIED_REPUTATION),
								(8, AMPLIFIED_OPERATIONAL),
								(9, AMPLIFIED_LEGAL),
								(10, CONSTANT),
								(11, CONFIDENTIALITY),
								(12, INTEGRITY);
					 */
					switch (formulaItem.getVariableTypeId().intValue()) {
						case 1: // CONSTANT
							currentValue = String.format("%,.2f", formulaItem.getValue());
							break;
						case 2: // PROCESS_REVENUE
							currentValue = formulaItem.getName() != null ? formulaItem.getName() : "Process revenue";
							break;
						case 3: // SYSTEM_NUMBER_OF_REC
							currentValue = formulaItem.getName() != null ? formulaItem.getName() : "System number of records";
							break;
						case 4: // ORGANIZATION_REVENUE
							currentValue = formulaItem.getName() != null ? formulaItem.getName() : "Organization revenue";
							break;
						case 5: // LIKELIHOOD
							currentValue = formulaItem.getName() != null ? formulaItem.getName() : "Likelihood";
							break;
						case 6: // IMPACT
							currentValue = formulaItem.getName() != null ? formulaItem.getName() : "Impact";
							break;
						case 7: // AMPLIFIED_REPUTATION
							currentValue = formulaItem.getName() != null ? formulaItem.getName() : "Amplified reputation";
							break;
						case 8: // AMPLIFIED_OPERATIONAL
							currentValue = formulaItem.getName() != null ? formulaItem.getName() : "Amplified operational";
							break;
						case 9: // AMPLIFIED_LEGAL
							currentValue = formulaItem.getName() != null ? formulaItem.getName() : "Amplified legal";
							break;
						case 10: // CONSTANT
							currentValue = String.format("%,.2f", formulaItem.getValue());
							break;
						case 11: // CONFIDENTIALITY
							currentValue = formulaItem.getName() != null ? formulaItem.getName() : "Confidentiality";
							break;
						case 12: // INTEGRITY
							currentValue = formulaItem.getName() != null ? formulaItem.getName() : "Integrity";
							break;
						case 13: // MARKET_CAPITALIZATION
							currentValue = formulaItem.getName() != null ? formulaItem.getName() : "Market capitalization";
							break;
						default:
							currentValue = formulaItem.getName() != null ? formulaItem.getName() :
								(formulaItem.getVariableType() != null ? formulaItem.getVariableType().getName() : "");
							/*
							currentValue = formulaItem.getName() != null ? formulaItem.getName() :
								(formulaItem.getVariableType() != null ?
									StringUtils.capitalize(StringUtils.replaceAll(StringUtils.lowerCase(formulaItem.getVariableType().getName()),"_", " "))
									: "");
							*/
					}
					i++;

					result += currentValue;
				}
			}
		}

		return result;
	}
}
