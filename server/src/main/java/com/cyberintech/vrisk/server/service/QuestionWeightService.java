package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dto.qual_metrics.QuestionWeightDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.QuestionWeights;
import com.cyberintech.vrisk.server.repository.jpa.QuestionWeightRepository;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Question Weights management Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-06
 */
@Service
public class QuestionWeightService {

	@Autowired
	private QuestionWeightRepository questionWeightRepository;

	/**
	 * Get Question Weights List
	 *
	 * @return Question Weights List
	 */
	public List<QuestionWeightDTO> getList() {

		List<QuestionWeights> items = questionWeightRepository.findAll();

		List<QuestionWeightDTO> itemDTOs = QuestionWeightDTO.fromEntitiesList(items, QuestionWeightDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Question Weight details
	 *
	 * @return Question Weight Details
	 */
	public QuestionWeights getQuestionWeight(Long itemId) {
		QuestionWeights itemDetails;

		try {
			itemDetails = questionWeightRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Question Weight not found in the database [{0}]", itemId));
		}

		return itemDetails;
	}


}
