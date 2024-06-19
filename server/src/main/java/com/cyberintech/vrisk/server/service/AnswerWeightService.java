package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dto.qual_metrics.AnswerWeightDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.AnswerWeight;
import com.cyberintech.vrisk.server.repository.jpa.AnswerWeightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Answer Weights management Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-06
 */
@Service
public class AnswerWeightService {

	@Autowired
	private AnswerWeightRepository questionWeightRepository;

	/**
	 * Get Answer Weights List
	 *
	 * @return Answer Weights List
	 */
	public List<AnswerWeightDTO> getList() {

		List<AnswerWeight> items = questionWeightRepository.getList();

		List<AnswerWeightDTO> itemDTOs = AnswerWeightDTO.fromEntitiesList(items, AnswerWeightDTO.class);

		return itemDTOs;
	}

}
