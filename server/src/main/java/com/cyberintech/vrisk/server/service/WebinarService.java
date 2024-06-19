package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FeedsFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.feeds.WebinarDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Webinars;
import com.cyberintech.vrisk.server.repository.jpa.WebinarsRepository;
import com.cyberintech.vrisk.server.rest.exception.*;
import com.cyberintech.vrisk.server.service.utils.CSVUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.*;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Webinars management Service. Implements basic CRUD.
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2023-01-16
 */
@Service
@Slf4j
public class WebinarService {

	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public static final String TITLE_HEADER = "Title";
	public static final String DESCRIPTION_HEADER = "Description";
	public static final String UID_HEADER = "Uid";
	public static final String URL_HEADER = "Url";
	public static final String DATE_HEADER = "Date";
	public static final String EXPIRATION_DATE_HEADER = "Expiration Date";
	@Autowired
	private WebinarsRepository webinarsRepository;


	/**
	 * Get Webinars List for current Filters
	 *
	 * @return Webinars List
	 */
	public FilteredResponse<FeedsFilter, WebinarDTO> getListFiltered(FilteredRequest<FeedsFilter> filteredRequest) {
		List<Webinars> items = null;
		Long count = 0l;
		String namePattern = "";
		if (filteredRequest.getFilter() != null && StringUtils.isNotEmpty(filteredRequest.getFilter().getName())) {
			namePattern = filteredRequest.getFilter().getName();
		}
		if (filteredRequest.getFilter() != null && Boolean.TRUE.equals(filteredRequest.getFilter().getExcludeExpired())) {
			items = webinarsRepository.getListByNameAndExpiryDate(namePattern, new Date(), filteredRequest.toPageRequest());
			count = webinarsRepository.getCountByNameAndExpiryDate(namePattern, new Date());
		} else {
			items = webinarsRepository.getListByName(namePattern, filteredRequest.toPageRequest());
			count = webinarsRepository.getCountByName(namePattern);
		}

		List<WebinarDTO> itemsDTOList = DTOBase.fromEntitiesList(items, WebinarDTO.class);
		FilteredResponse<FeedsFilter, WebinarDTO> filteredResponse = new FilteredResponse<>(filteredRequest);
		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());
		filteredResponse.setSort(filteredRequest.getSort());

		return filteredResponse;
	}

	/**
	 * Insert data from CSV file
	 */
	@Transactional
	public ImportResultDTO importWebinarsFromCSVFile(InputStream fileContentStream) {
		ImportResultDTO result = new ImportResultDTO();

		try {
			// Parse CSV file
			CSVParser csvParser = CSVUtils.createCSVParser(fileContentStream);
			List<CSVRecord> csvRecordList = csvParser.getRecords();
			if (csvParser.getHeaderMap().containsKey(TITLE_HEADER) && csvParser.getHeaderMap().containsKey(URL_HEADER)) {
				result = importWebinarsFromCSVItems(csvRecordList);
			} else {
				throw new BadRequestException("Title/Url header not found. Import Failed.");
			}

		} catch (IOException | ParseException e) {
			log.error("Failed to import webinar", e);
		}
		return result;
	}

	@Transactional
	public ImportResultDTO importWebinarsFromCSVItems(List<CSVRecord> csvRecordList) throws ParseException {

		ImportResultDTO result = new ImportResultDTO();


		for (CSVRecord csvRecord : csvRecordList) {
			// Accessing values by Header names
			String title = csvRecord.isMapped(TITLE_HEADER) ? csvRecord.get(TITLE_HEADER) : null;
			String description = csvRecord.isMapped(DESCRIPTION_HEADER) ? csvRecord.get(DESCRIPTION_HEADER) : null;
			String uid = csvRecord.isMapped(UID_HEADER) ? csvRecord.get(UID_HEADER) : null;
			String url = csvRecord.isMapped(URL_HEADER) ? csvRecord.get(URL_HEADER) : null;
			String date = csvRecord.isMapped(DATE_HEADER) ? csvRecord.get(DATE_HEADER) : null;
			String expirationDate = csvRecord.isMapped(EXPIRATION_DATE_HEADER) ? csvRecord.get(EXPIRATION_DATE_HEADER) : null;

			Optional<Webinars> webinarDetails = Optional.empty();
			if (StringUtils.isNotEmpty(uid)) {
				webinarDetails = webinarsRepository.findByUid(uid);
			}
			if (!webinarDetails.isPresent() && StringUtils.isNotEmpty(url)) {
				webinarDetails = webinarsRepository.findFirstByUrl(url);
			} else if (StringUtils.isEmpty(url)) {
				result.getIgnored().add(new ItemViewDTO(MessageFormat.format("Empty webinar url, {0}, {1}", description, url)));
				continue;
			}

			WebinarDTO webinarDTO = new WebinarDTO();
			if (webinarDetails.isPresent()) {
				webinarDTO = new WebinarDTO(webinarDetails.get());
			}

			if (StringUtils.isNotEmpty(title)) {
				webinarDTO.setTitle(title);
			} else {
				result.getIgnored().add(new ItemViewDTO(MessageFormat.format("Empty webinar title, {0}, {1}", description, url)));
				continue;
			}
			if (StringUtils.isNotEmpty(uid)) webinarDTO.setUid(uid);
			if (StringUtils.isNotEmpty(url)) webinarDTO.setUrl(url);
			webinarDTO.setDescription(description);

			if (StringUtils.isNotEmpty(expirationDate)) {
				try {
					webinarDTO.setExpiryDate(dateFormat.parse(convertDateFormat(expirationDate, dateFormat.toPattern())));
				} catch (ParseException e) {
					log.error("Unparseable date", e);
					result.getIgnored().add(new ItemViewDTO(MessageFormat.format("{0}, {1}, Unparseable expiration date", title, description)));
					//	throw new ForbiddenException("Unparseable date!", ApplicationExceptionCodes.UNPARSEABLE_DATE);
					continue;
				}
			}
			if (StringUtils.isEmpty(date)) {
				webinarDTO.setDate(new Date());
			} else {
				try {
					webinarDTO.setDate(dateFormat.parse(convertDateFormat(date, dateFormat.toPattern())));
				} catch (ParseException e) {
					log.error("Unparseable date", e);
					result.getIgnored().add(new ItemViewDTO(MessageFormat.format("{0}, {1}, Unparseable date", title, description)));
					//	throw new ForbiddenException("Unparseable date!", ApplicationExceptionCodes.UNPARSEABLE_DATE);
					continue;
				}

			}

			if (webinarDetails.isPresent()) {
				WebinarDTO webinarResult = update(webinarDTO);
				result.getUpdated().add(new ItemViewDTO(webinarResult.getId(), MessageFormat.format("{0}, {1}, {2}", webinarResult.getTitle(), webinarResult.getUrl(), webinarResult.getDate())));
			} else {
				WebinarDTO webinarResult = create(webinarDTO);
				result.getCreated().add(new ItemViewDTO(webinarResult.getId(), MessageFormat.format("{0}, {1}, {2}", webinarResult.getTitle(), webinarResult.getUrl(), webinarResult.getDate())));
			}
		}
		result.setStatus("SUCCESS");
		return result;
	}

	public String convertDateFormat(String dateToConvert, String pattern) {

		final List<String> dateFormats = Arrays.asList("yyyy-MM-dd", "dd-MM-yyyy", "dd.MM.yyyy", "yyyy.MM.dd", "dd/MM/yyyy", "yyyy/MM/dd");

		SimpleDateFormat sdf;
		String result = dateToConvert;

		if (StringUtils.isNotEmpty(dateToConvert)) {

			for (String formatItem : dateFormats) {
				sdf = new SimpleDateFormat(formatItem);
				sdf.setLenient(false);

				try {
					Date date = sdf.parse(dateToConvert);
					sdf.applyPattern(pattern);
					result = sdf.format(date);
					break;
				} catch (ParseException e) {
				}
			}
		}
		return result;
	}


	/**
	 * Get Webinar DTO details
	 *
	 * @return Webinar Details
	 */
	public WebinarDTO getDetails(Long itemId) {

		Webinars itemDetails;
		try {
			itemDetails = webinarsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Webinar not found in the database [{0}]", itemId));
		}

		WebinarDTO result = new WebinarDTO(itemDetails);

		return result;
	}

	/**
	 * Create new Webinar Item
	 *
	 * @return New Webinar Item
	 */
	public WebinarDTO create(WebinarDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}
		if ((newItemDTO.getUid() == null) || newItemDTO.getUid().isEmpty()) {
			String uid = UUID.randomUUID().toString();
			newItemDTO.setUid(uid);
		}
		Webinars webinar = new Webinars();
		webinar.setUid(newItemDTO.getUid());
		webinar.setName(newItemDTO.getTitle());
		webinar.setDescription(newItemDTO.getDescription());
		webinar.setUrl(newItemDTO.getUrl());
		webinar.setDate(newItemDTO.getDate());
		webinar.setExpiryDate(newItemDTO.getExpiryDate());
		webinar.setCreatedAt(new Date());
		webinar.setUpdatedAt(new Date());
		webinarsRepository.save(webinar);

		return new WebinarDTO(webinar);
	}

	/**
	 * Update Webinar Item
	 *
	 * @return Updated Webinar Item
	 */
	public WebinarDTO update(WebinarDTO newItemDTO) {
		if ((newItemDTO.getUid() == null) || newItemDTO.getUid().isEmpty()) {
			String uid = UUID.randomUUID().toString();
			newItemDTO.setUid(uid);
		}

		Webinars newsItem = webinarsRepository.findById(newItemDTO.getId()).get();
		newsItem.setUid(newItemDTO.getUid());
		newsItem.setName(newItemDTO.getTitle());
		newsItem.setDescription(newItemDTO.getDescription());
		newsItem.setUrl(newItemDTO.getUrl());
		newsItem.setDate(newItemDTO.getDate());
		newsItem.setExpiryDate(newItemDTO.getExpiryDate());
		newsItem.setUpdatedAt(new Date());
		webinarsRepository.save(newsItem);

		return new WebinarDTO(newsItem);

	}

	/**
	 * Get Template content for Download
	 */
	public ByteArrayInputStream getWebinarsDownloadData() {
		ByteArrayInputStream byteArrayInputStream;

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			CSVPrinter csvPrinter = createWebibarsCsvPrinter(outputStream);
			List<Webinars> items = webinarsRepository.getListByName("", PageRequest.of(0, 100, Sort.by(Sort.Order.desc("date"))));
			for (Webinars webinar : items) {
				/*
				if ((webinar.getUid() == null) || webinar.getUid().isEmpty()) {
					String uid = UUID.randomUUID().toString();
					webinar.setUid(uid);
				}
				*/
				csvPrinter.printRecord(
					webinar.getName(),
					webinar.getDescription(),
					webinar.getUid(),
					webinar.getUrl(),
					dateFormat.format(webinar.getDate()),
					dateFormat.format(webinar.getExpiryDate())
				);

			}
			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			log.error("Failed to generate CSV Template file for Webinars", e);
			throw new InternalServerErrorException("Failed to generate CSV Template file for Webinars");
		}
		return byteArrayInputStream;
	}

	private CSVPrinter createWebibarsCsvPrinter(ByteArrayOutputStream outputStream) throws IOException {
		Writer writer = new OutputStreamWriter(outputStream);
		CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(
			TITLE_HEADER,
			DESCRIPTION_HEADER,
			UID_HEADER,
			URL_HEADER,
			DATE_HEADER,
			EXPIRATION_DATE_HEADER
		);
		return new CSVPrinter(writer, csvFormat);
	}
}
