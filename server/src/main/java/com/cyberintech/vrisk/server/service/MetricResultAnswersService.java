package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.MetricResultAnswerModelDAO;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.QuestionFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.answers.MetricResultAnswerEditDTO;
import com.cyberintech.vrisk.server.model.dto.answers.MetricResultAnswerViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.MetricResultAnswers;
import com.cyberintech.vrisk.server.model.jpa.entity.QualitativeQuestionAnswers;
import com.cyberintech.vrisk.server.model.jpa.entity.QualitativeQuestions;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskModels;
import com.cyberintech.vrisk.server.repository.jpa.MetricResultAnswersRepository;
import com.cyberintech.vrisk.server.repository.jpa.QualitativeQuestionAnswerRepository;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Metric Resilts Answers management Service. Implements basic CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-26
 */
@Service
public class MetricResultAnswersService {

	@Autowired
	private MetricResultAnswersRepository metricResultAnswersRepository;

	@Autowired
	private QualitativeQuestionAnswerRepository answerRepository;

	@Autowired
	private RiskModelService riskModelService;

	@Autowired
	private QualitativeQuestionService qualitativeQuestionService;

	@Autowired
	private UserService userService;

	@Autowired
	private MetricResultAnswerModelDAO metricResultAnswerModelDAO;

	/**
	 * Get Metric Result Answers List
	 *
	 * @return Metric Result Answers List
	 */
	public List<MetricResultAnswerViewDTO> getListPaged() {
		List<MetricResultAnswers> items = metricResultAnswersRepository.findAll();

		List<MetricResultAnswerViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, MetricResultAnswerViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Metric Result Answer details
	 *
	 * @return Metric Result Answer Details
	 */
	public MetricResultAnswerViewDTO getDetails(Long itemId) {

		MetricResultAnswers itemDetails = getEntity(itemId);

		MetricResultAnswerViewDTO itemDTO = new MetricResultAnswerViewDTO(itemDetails);

		return itemDTO;
	}

	/**
	 * Get Metric Result Answer details
	 *
	 * @return Metric Result Answer Details
	 */
	public MetricResultAnswers getEntity(Long itemId) {
		MetricResultAnswers itemDetails;

		try {
			itemDetails = metricResultAnswersRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Answer not found in the database [{0}]", itemId), ApplicationExceptionCodes.METRIC_RISK_NOT_EXISTS);
		}

		// Verify Risk Model and Organization
		RiskModels riskModel = riskModelService.getRiskModel(itemDetails.getRiskModelId());

		return itemDetails;
	}

	/**
	 * Get Metric Result Answers List inside current Risk Model
	 *
	 * @return Metric Result Answers List
	 */
	public FilteredResponse<QuestionFilter, MetricResultAnswerViewDTO> getListByRiskModel(Long riskModelId, FilteredRequest<QuestionFilter> filteredRequest) {
		QuestionFilter filter = filteredRequest.getFilter();
		filter.setRiskModelId(riskModelId);

		PagedResult<MetricResultAnswerViewDTO> result = metricResultAnswerModelDAO.getItemsPageable(filter, filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<QuestionFilter, MetricResultAnswerViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Save list of Metric Result Answers
	 *
	 * @return New Metric Result Answer
	 */
	public List<MetricResultAnswerViewDTO> saveItems(List<MetricResultAnswerEditDTO> itemsList) {
		List<MetricResultAnswerViewDTO> result = new ArrayList<>();

		for (MetricResultAnswerEditDTO item : itemsList) {
			if (item.getId() == null) {
				MetricResultAnswers entity = metricResultAnswersRepository.getItemByQuestionIdAndRiskModelId(item.getQuestion().getId(), item.getRiskModelId());
				if (entity != null && entity.getId() != null) item.setId(entity.getId());
			}

			MetricResultAnswerViewDTO savedDTO;
			if (item.getId() != null) {
				savedDTO = update(item);
			} else {
				savedDTO = create(item);
			}
			result.add(savedDTO);
		}

		return result;
	}

	/**
	 * Create new Metric Result Answer
	 *
	 * @return New Metric Result Answer
	 */
	public MetricResultAnswerViewDTO create(MetricResultAnswerEditDTO newItemDTO) {

		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()), ApplicationExceptionCodes.CREATE_IS_NOT_ALLOWED_FOR_ITEM_WITH_EXISTING_ID);
		}

		RiskModels riskModel = riskModelService.getRiskModel(newItemDTO.getRiskModelId());

//		MetricResultAnswers newItem = newItemDTO.toEntity();
		MetricResultAnswers newItem = new MetricResultAnswers();
		newItem.setRiskModelId(riskModel.getId());
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		newItem.setMultiplier(newItemDTO.getMultiplier());
		applyEntityChanges(newItemDTO, newItem);

		MetricResultAnswers saveResult = metricResultAnswersRepository.save(newItem);

		// Save risk types
		metricResultAnswersRepository.flush();
		saveResult = metricResultAnswersRepository.findById(saveResult.getId()).get();

		MetricResultAnswerViewDTO result = new MetricResultAnswerViewDTO(saveResult);

		return result;
	}

	/**
	 * Update Metric Result Answers
	 *
	 * @return Updated Metric Result Answers
	 */
	public MetricResultAnswerViewDTO update(MetricResultAnswerEditDTO itemDTO) {

		MetricResultAnswerViewDTO result;

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		MetricResultAnswers existingItem = getEntity(itemDTO.getId());

		// Verify Risk Model and Organization Id
		RiskModels riskModel = riskModelService.getRiskModel(existingItem.getRiskModelId());

		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		MetricResultAnswers saveResult = metricResultAnswersRepository.save(existingItem);

		result = new MetricResultAnswerViewDTO(saveResult);

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(MetricResultAnswerEditDTO itemDTO, MetricResultAnswers entity) {

		QualitativeQuestions question = qualitativeQuestionService.getQualitativeQuestion(itemDTO.getQuestion().getId());
		QualitativeQuestionAnswers answer = (itemDTO.getAnswer() != null && itemDTO.getAnswer().getId() != null) ? answerRepository.findById(itemDTO.getAnswer().getId()).get() : null;

		entity.setQuestion(question);
		entity.setAnswer(answer);
		// entity.setMultiplier(itemDTO.getMultiplier());

		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Metric Result Answer
	 *
	 * @return ID of removed item
	 */
	public Long delete(Long itemId) {

		MetricResultAnswers existingItem = getEntity(itemId);

		metricResultAnswersRepository.delete(existingItem);

		return itemId;
	}

}
