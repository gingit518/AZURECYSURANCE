package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FeedsFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.feeds.PaceCourseDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.PaceCourse;
import com.cyberintech.vrisk.server.repository.jpa.PaceCourseRepository;
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
 * Pace Courses management Service. Implements basic CRUD.
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2023-01-16
 */

@Service
@Slf4j
public class PaceCourseService {

	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public static final String TITLE_HEADER = "Title";
	public static final String DESCRIPTION_HEADER = "Description";
	public static final String UID_HEADER = "Uid";
	public static final String URL_HEADER = "Url";
	public static final String DATE_HEADER = "Date";
	public static final String EXPIRATION_DATE_HEADER = "Expiration Date";

	@Autowired
	private PaceCourseRepository paceCourseRepository;


	/**
	 * Get Pace Course List for current Filters
	 *
	 * @return Pace Course List
	 */
	public FilteredResponse<FeedsFilter, PaceCourseDTO> getListFiltered(FilteredRequest<FeedsFilter> filteredRequest) {
		List<PaceCourse> items = null;
		Long count = 0l;
		String namePattern = "";
		if (filteredRequest.getFilter() != null && StringUtils.isNotEmpty(filteredRequest.getFilter().getName())) {
			namePattern = filteredRequest.getFilter().getName();
		}
		if (filteredRequest.getFilter() != null && Boolean.TRUE.equals(filteredRequest.getFilter().getExcludeExpired())) {
			items = paceCourseRepository.getListByNameAndExpiryDate(namePattern, new Date(), filteredRequest.toPageRequest());
			count = paceCourseRepository.getCountByNameAndExpiryDate(namePattern, new Date());
		} else {
			items = paceCourseRepository.getListByName(namePattern, filteredRequest.toPageRequest());
			count = paceCourseRepository.getCountByName(namePattern);
		}

		List<PaceCourseDTO> itemsDTOList = DTOBase.fromEntitiesList(items, PaceCourseDTO.class);
		FilteredResponse<FeedsFilter, PaceCourseDTO> filteredResponse = new FilteredResponse<>(filteredRequest);
		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());
		filteredResponse.setSort(filteredRequest.getSort());

		return filteredResponse;
	}

	/**
	 * Insert data from CSV file
	 */
	@Transactional
	public ImportResultDTO importPaceCoursesFromCSVFile(InputStream fileContentStream) {
		ImportResultDTO result = new ImportResultDTO();

		try {
			// Parse CSV file
			CSVParser csvParser = CSVUtils.createCSVParser(fileContentStream);
			List<CSVRecord> csvRecordList = csvParser.getRecords();
			if (csvParser.getHeaderMap().containsKey(TITLE_HEADER) && csvParser.getHeaderMap().containsKey(URL_HEADER)) {
				result = importPaceCoursesFromCSVItems(csvRecordList);
			} else {
				throw new BadRequestException("Title/Url header not found. Import Failed.");
			}
		} catch (IOException | ParseException e) {
			log.error("Failed to import pace course", e);
		}
		return result;
	}

	@Transactional
	public ImportResultDTO importPaceCoursesFromCSVItems(List<CSVRecord> csvRecordList) throws ParseException {

		ImportResultDTO result = new ImportResultDTO();

		for (CSVRecord csvRecord : csvRecordList) {
			// Accessing values by Header names
			String title = csvRecord.isMapped(TITLE_HEADER) ? csvRecord.get(TITLE_HEADER) : null;
			String description = csvRecord.isMapped(DESCRIPTION_HEADER) ? csvRecord.get(DESCRIPTION_HEADER) : null;
			String uid = csvRecord.isMapped(UID_HEADER) ? csvRecord.get(UID_HEADER) : null;
			String url = csvRecord.isMapped(URL_HEADER) ? csvRecord.get(URL_HEADER) : null;
			String date = csvRecord.isMapped(DATE_HEADER) ? csvRecord.get(DATE_HEADER) : null;
			String expirationDate = csvRecord.isMapped(EXPIRATION_DATE_HEADER) ? csvRecord.get(EXPIRATION_DATE_HEADER) : null;

			Optional<PaceCourse> paceCourseDetails = Optional.empty();
			if (StringUtils.isNotEmpty(uid)) {
				paceCourseDetails = paceCourseRepository.findByUid(uid);
			}
			if (!paceCourseDetails.isPresent() && StringUtils.isNotEmpty(url)) {
				paceCourseDetails = paceCourseRepository.findFirstByUrl(url);
			} else if (StringUtils.isEmpty(url)) {
				result.getIgnored().add(new ItemViewDTO(MessageFormat.format("Empty pace course url, {0}, {1}", description, url)));
				continue;
			}

			PaceCourseDTO paceCourseDTO = new PaceCourseDTO();
			if (paceCourseDetails.isPresent()) {
				paceCourseDTO = new PaceCourseDTO(paceCourseDetails.get());
			}

			if (StringUtils.isNotEmpty(title)) {
				paceCourseDTO.setTitle(title);
			} else {
				result.getIgnored().add(new ItemViewDTO(MessageFormat.format("Empty pace course title, {0}, {1}", description, url)));
				continue;
			}
			if (StringUtils.isNotEmpty(uid)) paceCourseDTO.setUid(uid);
			if (StringUtils.isNotEmpty(url)) paceCourseDTO.setUrl(url);
			paceCourseDTO.setDescription(description);

			if (StringUtils.isNotEmpty(expirationDate)) {
				try {
					paceCourseDTO.setExpiryDate(dateFormat.parse(convertDateFormat(expirationDate, dateFormat.toPattern())));
				} catch (ParseException e) {
					log.error("Unparseable date", e);
					result.getIgnored().add(new ItemViewDTO(MessageFormat.format("{0}, {1}, Unparseable expiration date", title, description)));
					//	throw new ForbiddenException("Unparseable date!", ApplicationExceptionCodes.UNPARSEABLE_DATE);
					continue;
				}
			}
			if (StringUtils.isEmpty(date)) {
				paceCourseDTO.setDate(new Date());
			} else {
				try {
					paceCourseDTO.setDate(dateFormat.parse(convertDateFormat(date, dateFormat.toPattern())));
				} catch (ParseException e) {
					log.error("Unparseable date", e);
					result.getIgnored().add(new ItemViewDTO(MessageFormat.format("{0}, {1}, Unparseable date", title, description)));
					//	throw new ForbiddenException("Unparseable date!", ApplicationExceptionCodes.UNPARSEABLE_DATE);
					continue;
				}

			}

			if (paceCourseDetails.isPresent()) {
				PaceCourseDTO paceCourseResult = update(paceCourseDTO);
				result.getUpdated().add(new ItemViewDTO(paceCourseResult.getId(), MessageFormat.format("{0}, {1}, {2}", paceCourseResult.getTitle(), paceCourseResult.getUrl(), paceCourseResult.getDate())));
			} else {
				PaceCourseDTO paceCourseResult = create(paceCourseDTO);
				result.getCreated().add(new ItemViewDTO(paceCourseResult.getId(), MessageFormat.format("{0}, {1}, {2}", paceCourseResult.getTitle(), paceCourseResult.getUrl(), paceCourseResult.getDate())));
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
	 * Get Pace Course DTO details
	 *
	 * @return Pace Course Details
	 */
	public PaceCourseDTO getDetails(Long itemId) {

		PaceCourse itemDetails;
		try {
			itemDetails = paceCourseRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Pace course not found in the database [{0}]", itemId));
		}

		PaceCourseDTO result = new PaceCourseDTO(itemDetails);

		return result;
	}

	/**
	 * Create new Pace Course Item
	 *
	 * @return New Pace Course Item
	 */
	public PaceCourseDTO create(PaceCourseDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}
		if ((newItemDTO.getUid() == null) || newItemDTO.getUid().isEmpty()) {
			String uid = UUID.randomUUID().toString();
			newItemDTO.setUid(uid);
		}
		PaceCourse paceCourse = new PaceCourse();
		paceCourse.setUid(newItemDTO.getUid());
		paceCourse.setName(newItemDTO.getTitle());
		paceCourse.setDescription(newItemDTO.getDescription());
		paceCourse.setUrl(newItemDTO.getUrl());
		paceCourse.setDate(newItemDTO.getDate());
		paceCourse.setExpiryDate(newItemDTO.getDate());
		paceCourse.setCreatedAt(new Date());
		paceCourse.setUpdatedAt(new Date());
		paceCourseRepository.save(paceCourse);

		return new PaceCourseDTO(paceCourse);

	}

	/**
	 * Update Pace Course Item
	 *
	 * @return Updated Pace Course Item
	 */
	public PaceCourseDTO update(PaceCourseDTO newItemDTO) {
		if ((newItemDTO.getUid() == null) || newItemDTO.getUid().isEmpty()) {
			String uid = UUID.randomUUID().toString();
			newItemDTO.setUid(uid);
		}

		PaceCourse newsItem = paceCourseRepository.findById(newItemDTO.getId()).get();
		newsItem.setUid(newItemDTO.getUid());
		newsItem.setName(newItemDTO.getTitle());
		newsItem.setDescription(newItemDTO.getDescription());
		newsItem.setUrl(newItemDTO.getUrl());
		newsItem.setDate(newItemDTO.getDate());
		newsItem.setExpiryDate(newItemDTO.getExpiryDate());
		newsItem.setUpdatedAt(new Date());
		paceCourseRepository.save(newsItem);

		return new PaceCourseDTO(newsItem);

	}

	/**
	 * Get Template content for Download
	 */
	public ByteArrayInputStream getPaceCoursesDownloadData() {
		ByteArrayInputStream byteArrayInputStream = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-dd-MM");

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			CSVPrinter csvPrinter = createPaceCoursesCsvPrinter(outputStream);
			List<PaceCourse> items = paceCourseRepository.getListByName("", PageRequest.of(0, 100, Sort.by(Sort.Order.desc("date"))));
			for (PaceCourse paceCourse : items) {
				/*
				if ((paceCourse.getUid() == null) || paceCourse.getUid().isEmpty()) {
					String uid = UUID.randomUUID().toString();
					paceCourse.setUid(uid);
				}
				*/
				csvPrinter.printRecord(
					paceCourse.getName(),
					paceCourse.getDescription(),
					paceCourse.getUid(),
					paceCourse.getUrl(),
					dateFormat.format(paceCourse.getDate()),
					dateFormat.format(paceCourse.getExpiryDate())
				);

			}
			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			log.error("Failed to generate CSV Template file for Pace Courses", e);
			throw new InternalServerErrorException("Failed to generate CSV Template file for Pace Courses");
		}
		return byteArrayInputStream;
	}

	private CSVPrinter createPaceCoursesCsvPrinter(ByteArrayOutputStream outputStream) throws IOException {
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
