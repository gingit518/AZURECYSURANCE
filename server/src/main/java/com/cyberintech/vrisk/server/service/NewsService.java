package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FeedsFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.feeds.NewsFeedDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.News;
import com.cyberintech.vrisk.server.repository.jpa.NewsRepository;
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
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * News management Service. Implements basic CRUD.
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2023-01-12
 */
@Service
@Slf4j
public class NewsService {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public static final String TITLE_HEADER = "Title";
	public static final String DESCRIPTION_HEADER = "Description";
	public static final String UID_HEADER = "Uid";
	public static final String URL_HEADER = "Url";
	public static final String DATE_HEADER = "Date";
	public static final String EXPIRATION_DATE_HEADER = "Expiration Date";


	@Autowired
	private NewsRepository newsRepository;


	/**
	 * Get News List for current Filters
	 *
	 * @return News List
	 */
	public FilteredResponse<FeedsFilter, NewsFeedDTO> getListFiltered(FilteredRequest<FeedsFilter> filteredRequest) {
		List<News> items = null;
		Long count = 0l;
		String namePattern = "";
		if (filteredRequest.getFilter() != null && StringUtils.isNotEmpty(filteredRequest.getFilter().getName())) {
			namePattern = filteredRequest.getFilter().getName();
		}
		if (filteredRequest.getFilter() != null && Boolean.TRUE.equals(filteredRequest.getFilter().getExcludeExpired())) {
			items = newsRepository.getListByNameAndExpiryDate(namePattern, new Date(), filteredRequest.toPageRequest());
			count = newsRepository.getCountByNameAndExpiryDate(namePattern, new Date());
		} else {
			items = newsRepository.getListByName(namePattern, filteredRequest.toPageRequest());
			count = newsRepository.getCountByName(namePattern);
		}

		List<NewsFeedDTO> itemsDTOList = DTOBase.fromEntitiesList(items, NewsFeedDTO.class);

		FilteredResponse<FeedsFilter, NewsFeedDTO> filteredResponse = new FilteredResponse<>(filteredRequest);
		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());
		filteredResponse.setSort(filteredRequest.getSort());

		return filteredResponse;
	}

	/**
	 * Insert data from CSV file
	 */
	@Transactional
	public ImportResultDTO importNewsFromCSVFile(InputStream fileContentStream) {
		ImportResultDTO result = new ImportResultDTO();

		try {
			// Parse CSV file
			CSVParser csvParser = CSVUtils.createCSVParser(fileContentStream);
			List<CSVRecord> csvRecordList = csvParser.getRecords();
			if (csvParser.getHeaderMap().containsKey(TITLE_HEADER) && csvParser.getHeaderMap().containsKey(URL_HEADER)) {
				result = importFeedsFromCSVItems(csvRecordList);
			} else {
				throw new BadRequestException("Title/Url header not found. Import Failed.");
			}

		} catch (IOException | ParseException e) {
			log.error("Failed to import news", e);
		}
		return result;
	}

	@Transactional
	public ImportResultDTO importFeedsFromCSVItems(List<CSVRecord> csvRecordList) throws ParseException {

		ImportResultDTO result = new ImportResultDTO();

		for (CSVRecord csvRecord : csvRecordList) {
			// Accessing values by Header names
			String title = csvRecord.isMapped(TITLE_HEADER) ? csvRecord.get(TITLE_HEADER) : null;
			String description = csvRecord.isMapped(DESCRIPTION_HEADER) ? csvRecord.get(DESCRIPTION_HEADER) : null;
			String uid = csvRecord.isMapped(UID_HEADER) ? csvRecord.get(UID_HEADER) : null;
			String url = csvRecord.isMapped(URL_HEADER) ? csvRecord.get(URL_HEADER) : null;
			String date = csvRecord.isMapped(DATE_HEADER) ? csvRecord.get(DATE_HEADER) : null;
			String expirationDate = csvRecord.isMapped(EXPIRATION_DATE_HEADER) ? csvRecord.get(EXPIRATION_DATE_HEADER) : null;

			Optional<News> newsDetails = Optional.empty();
			if (StringUtils.isNotEmpty(uid)) {
				newsDetails = newsRepository.findByUid(uid);
			}

			if (StringUtils.isEmpty(uid) && StringUtils.isNotEmpty(url)) {
				newsDetails = newsRepository.findFirstByUrl(url);
			} else if (StringUtils.isEmpty(url)) {
				result.getIgnored().add(new ItemViewDTO(MessageFormat.format("Empty news url, {0}, {1}", description, url)));
				continue;
			}

			NewsFeedDTO newsDTO = new NewsFeedDTO();
			if (newsDetails.isPresent()) {
				newsDTO = new NewsFeedDTO(newsDetails.get());
			}

			if (StringUtils.isNotEmpty(title)) {
				newsDTO.setTitle(title);
			} else {
				result.getIgnored().add(new ItemViewDTO(MessageFormat.format("Empty news title, {0}, {1}", description, url)));
				continue;
			}
			if (StringUtils.isNotEmpty(uid)) newsDTO.setUid(uid);
			if (StringUtils.isNotEmpty(url)) newsDTO.setUrl(url);
			newsDTO.setDescription(description);

			if (StringUtils.isNotEmpty(expirationDate)) {
				try {
					newsDTO.setExpiryDate(dateFormat.parse(convertDateFormat(expirationDate, dateFormat.toPattern())));
				} catch (Exception e) {
					log.error("Unparseable date", e);
					result.getIgnored().add(new ItemViewDTO(MessageFormat.format("{0}, {1}, Unparseable expiration date", title, description)));
					//	throw new ForbiddenException("Unparseable date!", ApplicationExceptionCodes.UNPARSEABLE_DATE);
					continue;
				}
			}
			if (StringUtils.isEmpty(date)) {
				newsDTO.setDate(new Date());
			} else {
				try {
					newsDTO.setDate(dateFormat.parse(convertDateFormat(date, dateFormat.toPattern())));
				} catch (Exception e) {
					log.error("Unparseable date", e);
					result.getIgnored().add(new ItemViewDTO(MessageFormat.format("{0}, {1}, Unparseable date", title, description)));
					//	throw new ForbiddenException("Unparseable date!", ApplicationExceptionCodes.UNPARSEABLE_DATE);
					continue;
				}

			}

			if (newsDetails.isPresent()) {
				NewsFeedDTO newsResult = update(newsDTO);
				result.getUpdated().add(new ItemViewDTO(newsResult.getId(), MessageFormat.format("{0}, {1}, {2}", newsResult.getTitle(), newsResult.getUrl(), newsResult.getDate())));
			} else {
				NewsFeedDTO newsResult = create(newsDTO);
				result.getCreated().add(new ItemViewDTO(newsResult.getId(), MessageFormat.format("{0}, {1}, {2}", newsResult.getTitle(), newsResult.getUrl(), newsResult.getDate())));
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
	 * Get News DTO details
	 *
	 * @return News Details
	 */
	public NewsFeedDTO getDetails(Long itemId) {

		News itemDetails;
		try {
			itemDetails = newsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("News not found in the database [{0}]", itemId));
		}

		NewsFeedDTO result = new NewsFeedDTO(itemDetails);

		return result;
	}

	/**
	 * Create new News Item
	 *
	 * @return New News Item
	 */
	public NewsFeedDTO create(NewsFeedDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}
		if ((newItemDTO.getUid() == null) || newItemDTO.getUid().isEmpty()) {
			String uid = UUID.randomUUID().toString();
			newItemDTO.setUid(uid);
		}
		News newsItem = new News();
		newsItem.setUid(newItemDTO.getUid());
		newsItem.setName(newItemDTO.getTitle());
		newsItem.setDescription(newItemDTO.getDescription());
		newsItem.setUrl(newItemDTO.getUrl());
		newsItem.setDate(newItemDTO.getDate());
		newsItem.setExpiryDate(newItemDTO.getExpiryDate());
		newsItem.setCreatedAt(new Date());
		newsItem.setUpdatedAt(new Date());
		newsRepository.save(newsItem);

		return new NewsFeedDTO(newsItem);

	}

	/**
	 * Update News Item
	 *
	 * @return Updated News Item
	 */
	public NewsFeedDTO update(NewsFeedDTO newItemDTO) {
		if ((newItemDTO.getUid() == null) || newItemDTO.getUid().isEmpty()) {
			String uid = UUID.randomUUID().toString();
			newItemDTO.setUid(uid);
		}

		News newsItem = newsRepository.findById(newItemDTO.getId()).get();
		newsItem.setUid(newItemDTO.getUid());
		newsItem.setName(newItemDTO.getTitle());
		newsItem.setDescription(newItemDTO.getDescription());
		newsItem.setUrl(newItemDTO.getUrl());
		newsItem.setDate(newItemDTO.getDate());
		newsItem.setExpiryDate(newItemDTO.getExpiryDate());
		newsItem.setUpdatedAt(new Date());
		newsRepository.save(newsItem);

		return new NewsFeedDTO(newsItem);

	}

	/**
	 * Get Template content for Download
	 */
	public ByteArrayInputStream getNewsDownloadData() {
		ByteArrayInputStream byteArrayInputStream = null;
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			CSVPrinter csvPrinter = createFeedsCsvPrinter(outputStream);
			// List<News> items = newsRepository.findAll();
			List<News> items = newsRepository.getListByName("", PageRequest.of(0, 100, Sort.by(Sort.Order.desc("date"))));
			for (News news : items) {
				/*
				if ((news.getUid() == null) || news.getUid().isEmpty()) {
					String uid = UUID.randomUUID().toString();
					news.setUid(uid);
				}
				*/
				csvPrinter.printRecord(
					news.getName(),
					news.getDescription(),
					news.getUid(),
					news.getUrl(),
					dateFormat.format(news.getDate()),
					dateFormat.format(news.getExpiryDate())
				);
			}
			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			log.error("Failed to generate CSV Template file for News", e);
			throw new InternalServerErrorException("Failed to generate CSV Template file for News");
		}

		return byteArrayInputStream;
	}

	private CSVPrinter createFeedsCsvPrinter(ByteArrayOutputStream outputStream) throws IOException {
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


