package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FeedsFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.feeds.WhatsNewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.WhatsNew;
import com.cyberintech.vrisk.server.repository.jpa.WhatsNewRepository;
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
 * What's New management Service. Implements basic CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2023-01-16
 */
@Service
@Slf4j
public class WhatsNewService {
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public static final String TITLE_HEADER = "Title";
	public static final String DESCRIPTION_HEADER = "Description";
	public static final String UID_HEADER = "Uid";
	public static final String URL_HEADER = "Url";
	public static final String DATE_HEADER = "Date";
	public static final String EXPIRATION_DATE_HEADER = "Expiration Date";

	@Autowired
	private WhatsNewRepository whatsNewRepository;

	/**
	 * Get What's New List for current Filters
	 *
	 * @return What's New List
	 */
	public FilteredResponse<FeedsFilter, WhatsNewDTO> getListFiltered(FilteredRequest<FeedsFilter> filteredRequest) {
		List<WhatsNew> items = null;
		Long count = 0l;

		String namePattern = "";
		if (filteredRequest.getFilter() != null && StringUtils.isNotEmpty(filteredRequest.getFilter().getName())) {
			namePattern = filteredRequest.getFilter().getName();
		}

		if (filteredRequest.getFilter() != null && Boolean.TRUE.equals(filteredRequest.getFilter().getExcludeExpired())) {
			items = whatsNewRepository.getListByNameAndExpiryDate(namePattern, new Date(), filteredRequest.toPageRequest());
			count = whatsNewRepository.getCountByNameAndExpiryDate(namePattern, new Date());
		} else {
			items = whatsNewRepository.getListByName(namePattern, filteredRequest.toPageRequest());
			count = whatsNewRepository.getCountByName(namePattern);
		}

		List<WhatsNewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, WhatsNewDTO.class);

		FilteredResponse<FeedsFilter, WhatsNewDTO> filteredResponse = new FilteredResponse<>(filteredRequest);
		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());
		filteredResponse.setSort(filteredRequest.getSort());

		return filteredResponse;
	}

	/**
	 * Insert data from CSV file
	 */
	@Transactional
	public ImportResultDTO importWhatsNewFromCSVFile(InputStream fileContentStream) {
		ImportResultDTO result = new ImportResultDTO();

		try {
			// Parse CSV file
			CSVParser csvParser = CSVUtils.createCSVParser(fileContentStream);
			List<CSVRecord> csvRecordList = csvParser.getRecords();
			if (csvParser.getHeaderMap().containsKey(TITLE_HEADER) && csvParser.getHeaderMap().containsKey(URL_HEADER)) {
				result = importWhatsNewFromCSVItems(csvRecordList);
			} else {
				throw new BadRequestException("Title/Url header not found. Import Failed.");
			}

		} catch (IOException | ParseException e) {
			log.error("Failed to import what's new", e);
		}
		return result;
	}

	@Transactional
	public ImportResultDTO importWhatsNewFromCSVItems(List<CSVRecord> csvRecordList) throws ParseException {

		ImportResultDTO result = new ImportResultDTO();


		for (CSVRecord csvRecord : csvRecordList) {
			// Accessing values by Header names
			String title = csvRecord.isMapped(TITLE_HEADER) ? csvRecord.get(TITLE_HEADER) : null;
			String description = csvRecord.isMapped(DESCRIPTION_HEADER) ? csvRecord.get(DESCRIPTION_HEADER) : null;
			String uid = csvRecord.isMapped(UID_HEADER) ? csvRecord.get(UID_HEADER) : null;
			String url = csvRecord.isMapped(URL_HEADER) ? csvRecord.get(URL_HEADER) : null;
			String date = csvRecord.isMapped(DATE_HEADER) ? csvRecord.get(DATE_HEADER) : null;
			String expirationDate = csvRecord.isMapped(EXPIRATION_DATE_HEADER) ? csvRecord.get(EXPIRATION_DATE_HEADER) : null;

			Optional<WhatsNew> whatsNewDetails = Optional.empty();
			if (StringUtils.isNotEmpty(uid)) {
				whatsNewDetails = whatsNewRepository.findByUid(uid);
			}
			if (StringUtils.isEmpty(uid) && StringUtils.isNotEmpty(url)) {
				whatsNewDetails = whatsNewRepository.findFirstByUrl(url);
			}
			if (!whatsNewDetails.isPresent()) {
				whatsNewDetails = whatsNewRepository.findFirstByName(title);
			}

			if (!whatsNewDetails.isPresent() && StringUtils.isEmpty(url)) {
				result.getIgnored().add(new ItemViewDTO(MessageFormat.format("Empty what's new url, {0}, {1}", description, url)));
				continue;
			}

			WhatsNewDTO whatsNewDTO = new WhatsNewDTO();
			if (whatsNewDetails.isPresent()) {
				whatsNewDTO = new WhatsNewDTO(whatsNewDetails.get());
			}

			if (StringUtils.isNotEmpty(title)) {
				whatsNewDTO.setTitle(title);
			} else {
				result.getIgnored().add(new ItemViewDTO(MessageFormat.format("Empty what's new title, {0}, {1}", description, url)));
				continue;
			}
			if (StringUtils.isNotEmpty(uid)) whatsNewDTO.setUid(uid);
			if (StringUtils.isNotEmpty(url)) whatsNewDTO.setUrl(url);
			whatsNewDTO.setDescription(description);

			if (StringUtils.isNotEmpty(expirationDate)) {
				try {
					whatsNewDTO.setExpiryDate(dateFormat.parse(convertDateFormat(expirationDate, dateFormat.toPattern())));
				} catch (ParseException e) {
					log.error("Unparseable date", e);
					result.getIgnored().add(new ItemViewDTO(MessageFormat.format("{0}, {1}, Unparseable expiration date", title, description)));
					//	throw new ForbiddenException("Unparseable date!", ApplicationExceptionCodes.UNPARSEABLE_DATE);
					continue;
				}
			}
			if (StringUtils.isEmpty(date)) {
				whatsNewDTO.setDate(new Date());
			} else {
				try {
					whatsNewDTO.setDate(dateFormat.parse(convertDateFormat(date, dateFormat.toPattern())));
				} catch (ParseException e) {
					log.error("Unparseable date", e);
					result.getIgnored().add(new ItemViewDTO(MessageFormat.format("{0}, {1}, Unparseable date", title, description)));
					//	throw new ForbiddenException("Unparseable date!", ApplicationExceptionCodes.UNPARSEABLE_DATE);
					continue;
				}

			}

			if (whatsNewDetails.isPresent()) {
				WhatsNewDTO whatsNewResult = update(whatsNewDTO);
				result.getUpdated().add(new ItemViewDTO(whatsNewResult.getId(), MessageFormat.format("{0}, {1}, {2}", whatsNewResult.getTitle(), whatsNewResult.getUrl(), whatsNewResult.getDate())));
			} else {
				WhatsNewDTO whatsNewResult = create(whatsNewDTO);
				result.getCreated().add(new ItemViewDTO(whatsNewResult.getId(), MessageFormat.format("{0}, {1}, {2}", whatsNewResult.getTitle(), whatsNewResult.getUrl(), whatsNewResult.getDate())));
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
	 * Get What's New DTO details
	 *
	 * @return What's New Details
	 */
	public WhatsNewDTO getDetails(Long itemId) {

		WhatsNew itemDetails;
		try {
			itemDetails = whatsNewRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("What's new not found in the database [{0}]", itemId));
		}

		WhatsNewDTO result = new WhatsNewDTO(itemDetails);

		return result;
	}

	/**
	 * Create new What's New Item
	 *
	 * @return New What's New Item
	 */
	public WhatsNewDTO create(WhatsNewDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}
		if ((newItemDTO.getUid() == null) || newItemDTO.getUid().isEmpty()) {
			String uid = UUID.randomUUID().toString();
			newItemDTO.setUid(uid);
		}
		WhatsNew whatsNew = new WhatsNew();
		whatsNew.setUid(newItemDTO.getUid());
		whatsNew.setName(newItemDTO.getTitle());
		whatsNew.setDescription(newItemDTO.getDescription());
		whatsNew.setUrl(newItemDTO.getUrl());
		whatsNew.setDate(newItemDTO.getDate());
		whatsNew.setExpiryDate(newItemDTO.getExpiryDate());
		whatsNew.setCreatedAt(new Date());
		whatsNew.setUpdatedAt(new Date());
		whatsNewRepository.save(whatsNew);

		return new WhatsNewDTO(whatsNew);
	}

	/**
	 * Update What's New Item
	 *
	 * @return Updated What's New Item
	 */
	public WhatsNewDTO update(WhatsNewDTO newItemDTO) {
		if ((newItemDTO.getUid() == null) || newItemDTO.getUid().isEmpty()) {
			String uid = UUID.randomUUID().toString();
			newItemDTO.setUid(uid);
		}

		WhatsNew newsItem = whatsNewRepository.findById(newItemDTO.getId()).get();
		newsItem.setUid(newItemDTO.getUid());
		newsItem.setName(newItemDTO.getTitle());
		newsItem.setDescription(newItemDTO.getDescription());
		newsItem.setUrl(newItemDTO.getUrl());
		newsItem.setDate(newItemDTO.getDate());
		newsItem.setExpiryDate(newItemDTO.getExpiryDate());
		newsItem.setUpdatedAt(new Date());
		whatsNewRepository.save(newsItem);

		return new WhatsNewDTO(newsItem);

	}

	/**
	 * Get Template content for Download
	 */
	public ByteArrayInputStream getWhatsNewDownloadData() {
		ByteArrayInputStream byteArrayInputStream;

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			CSVPrinter csvPrinter = createWhatsNewCsvPrinter(outputStream);
			List<WhatsNew> items = whatsNewRepository.getListByName("", PageRequest.of(0, 100, Sort.by(Sort.Order.desc("date"))));
			for (WhatsNew whatsNew : items) {
				/*
				if ((whatsNew.getUid() == null) || whatsNew.getUid().isEmpty()) {
					String uid = UUID.randomUUID().toString();
					whatsNew.setUid(uid);
				}
				*/
				csvPrinter.printRecord(
					whatsNew.getName(),
					whatsNew.getDescription(),
					whatsNew.getUid(),
					whatsNew.getUrl(),
					dateFormat.format(whatsNew.getDate()),
					dateFormat.format(whatsNew.getExpiryDate())
				);

			}
			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			log.error("Failed to generate CSV Template file for What's New", e);
			throw new InternalServerErrorException("Failed to generate CSV Template file for What's New");
		}
		return byteArrayInputStream;
	}

	private CSVPrinter createWhatsNewCsvPrinter(ByteArrayOutputStream outputStream) throws IOException {
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
