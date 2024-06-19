package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.language.LanguageViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.service.utils.CSVUtils;
import com.google.common.collect.ImmutableMap;
import io.keen.client.java.KeenClient;
import io.keen.client.java.KeenProject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * KeenIO Analytics Streaming Service.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-10-23
 */
@Service
@Slf4j
@Profile("keen")
public class KeenIOService {

	@Autowired
	private KeenClient keenClient;

	@Autowired
	private RiskModelRepository riskModelRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private QuestionAnswersForVendorRepository questionAnswersForVendorRepository;

	@Autowired
	private QuestionAnswersForSystemRepository questionAnswersForSystemRepository;

	/**
	 * Adding Vendor Question Event to the Collection
	 *
	 * @param questionAnswersForVendor
	 */
	@Async
	public void addQuestionAnswerEvent(QuestionAnswersForVendor questionAnswersForVendor) {
		Map<String, Object> event = new HashMap<>();

		// Build Common KeenIO Data Details
		buildOrganizationAndRiskModelDetails(event, questionAnswersForVendor.getQuestion().getRiskModelId());
		buildQuestionAnswerDetails(event, questionAnswersForVendor.getQuestion(), questionAnswersForVendor.getAnswer(), questionAnswersForVendor.getDocument());

		// Add Vendor Details
		event.put("vendorId", questionAnswersForVendor.getVendor().getId());
		event.put("vendorName", questionAnswersForVendor.getVendor().getName());

		// Put KeenIO Timestamp parameter
		Map<String, Object> keenProperties = ImmutableMap.<String, Object>builder()
			.put("timestamp", DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.ofInstant(questionAnswersForVendor.getCreatedAt().toInstant(), ZoneId.systemDefault())))
			.build();

		keenClient.addEvent("qualitative_question_answers", event, keenProperties);
	}

	/**
	 * Adding System Question Event to the Collection
	 *
	 * @param questionAnswersForSystem
	 */
	@Async
	public void addQuestionAnswerEvent(QuestionAnswersForSystem questionAnswersForSystem) {
		Map<String, Object> event = new HashMap<>();

		// Build Common KeenIO Data Details
		buildOrganizationAndRiskModelDetails(event, questionAnswersForSystem.getQuestion().getRiskModelId());
		buildQuestionAnswerDetails(event, questionAnswersForSystem.getQuestion(), questionAnswersForSystem.getAnswer(), questionAnswersForSystem.getDocument());

		// Add Vendor Details
		event.put("systemId", questionAnswersForSystem.getSystem().getId());
		event.put("systemName", questionAnswersForSystem.getSystem().getName());

		// Put KeenIO Timestamp parameter
		Map<String, Object> keenProperties = ImmutableMap.<String, Object>builder()
			.put("timestamp", DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.ofInstant(questionAnswersForSystem.getCreatedAt().toInstant(), ZoneId.systemDefault())))
			.build();

		keenClient.addEvent("qualitative_question_answers", event, keenProperties);
	}

	/**
	 * Adding System Question Event to the Collection
	 *
	 * @param itemId
	 */
	@Async
	@Transactional
	@Deprecated
	public void addQuestionAnswer4SystemEvent(Long itemId) {
		QuestionAnswersForSystem questionAnswersForSystem = questionAnswersForSystemRepository.findById(itemId).get();
		addQuestionAnswerEvent(questionAnswersForSystem);
	}

	/**
	 * Adding Vendor Question Event to the Collection
	 *
	 * @param itemId
	 */
	@Async
	@Transactional
	@Deprecated
	public void addQuestionAnswer4VendorEvent(Long itemId) {
		QuestionAnswersForVendor questionAnswersForVendor = questionAnswersForVendorRepository.findById(itemId).get();
		addQuestionAnswerEvent(questionAnswersForVendor);
	}

	private void buildOrganizationAndRiskModelDetails(Map<String, Object> event, Long riskModelId) {

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

		event.put("organizationId", organization.getId());
		event.put("organizationName", organization.getName());
		event.put("riskModelId", riskModel.getId());
		event.put("riskModelName", riskModel.getName());
	}

	private void buildQuestionAnswerDetails(Map<String, Object> event, QualitativeQuestions question, QualitativeQuestionAnswers answer, Documents document) {

		event.put("questionId", question.getId());
		event.put("question", question.getQuestion());
		event.put("question", question.getQuestion());
		if (question.getVendorType() != null) event.put("questionType", question.getVendorType().name());

		if (answer != null) {
			event.put("answerId", answer.getId());
			event.put("answer", answer.getAnswer());
			event.put("answerWeight", answer.getAnswerWeight().getValue());
			event.put("answerWeightName", answer.getAnswerWeight().getName());
		}

		if (document != null) {
			event.put("document", document.getFileName());
			event.put("documentType", document.getFileType());
			event.put("documentSize", document.getFileSize());
		}
	}

	/**
	 * Insert GDPR data from CSV file
	 */
	public ImportResultDTO importKeenCollectionFromCSVFile(MultipartFile file) {

		String[] filePieces = StringUtils.split(file.getOriginalFilename(), ".");
		String collectionName = filePieces[0].toLowerCase();

		ImportResultDTO result = new ImportResultDTO();
		try {
			DateTimeFormatter iso8601dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

			CSVParser csvParser = CSVUtils.createCSVParser(file.getInputStream());
			List<CSVRecord> csvRecordList = csvParser.getRecords();
			Map<String, Integer> headersMap = csvParser.getHeaderMap();
			for (CSVRecord csvRecord : csvRecordList) {
				// Accessing values by Header names
				Map<String, Object> keenProperties = new HashMap<>();
				Map<String, Object> event = new HashMap<>();
				for (Map.Entry<String, Integer> header: headersMap.entrySet()) {
					String valueString = csvRecord.get(header.getValue());
					String variableName = header.getKey();

					// Ignore empty values
					if (StringUtils.isEmpty(valueString)) continue;

					event.put(variableName, valueString);

					try {

						if (StringUtils.isNotEmpty(valueString)) {
							String dValueString = valueString;
							if (StringUtils.startsWith(valueString, "$") && valueString.length() > 1) {
								dValueString = valueString.substring(1).replaceAll(",", "");
							}

							if (NumberUtils.isParsable(dValueString)) {
								Double valueDouble = Double.valueOf(dValueString);
								event.put(variableName, valueDouble);
							}
						}
					} catch (NumberFormatException e) {
					}

					try {
						if (StringUtils.isNotEmpty(valueString)) {
							// Date valueDate = DateUtils.parseDate(valueString, "yyyy-MM-dd HH:mm:ss");
							// 10/8/2018 0:00
							Date valueDate = DateUtils.parseDate(valueString, "M/d/yyyy H:mm");

							String valueDateString = iso8601dateTimeFormat.format(ZonedDateTime.ofInstant(valueDate.toInstant(), ZoneId.systemDefault()));
							event.put(variableName, valueDateString);

							if (StringUtils.isNotEmpty(valueDateString)) {
								if (!keenProperties.containsKey("addons")) keenProperties.put("addons", new ArrayList<>());

								Map<String, Object> addon = ImmutableMap.<String, Object>builder()
									.put("name", "keen:date_time_parser")
									.put("input", ImmutableMap.<String, Object>builder().put("date_time", variableName).build())
									.put("output", variableName + "_DATETIME")
									.build();
								((List) keenProperties.get("addons")).add(addon);
							}
						}
					} catch (ParseException e) {
					}
				}

				keenClient.addEventAsync(collectionName, event, keenProperties);
			}

		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}

		return result;
	}

}
