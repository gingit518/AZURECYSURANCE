package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.*;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.GDPRFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.gdpr.*;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.DataTypeDomain;
import com.cyberintech.vrisk.server.model.jpa.domains.RoleType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.InternalServerErrorException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import com.cyberintech.vrisk.server.service.utils.CSVUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.*;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GDPR Items management Service. Implements basic CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-24
 */
@Service
@Slf4j
public class GDPRItemsService {

	public static final String GDPR_CHAPTER_REF_HEADER = "Chapter #";
	public static final String GDPR_CHAPTER_NAME_HEADER = "Chapter Name";
	public static final String GDPR_SECTION_REF_HEADER = "Section #";
	public static final String GDPR_SECTION_NAME_HEADER = "Section Name";
	public static final String GDPR_ARTICLE_REF_HEADER = "Article #";
	public static final String GDPR_ARTICLE_NAME_HEADER = "Article Name";
	public static final String GDPR_ARTICLE_DESCRIPTION_HEADER = "Article Description";
	public static final String GDPR_ARTICLE_BEST_PRACTICE_HEADER = "GDPR Best Practice";
	public static final String GDPR_PARAGRAPH_HEADER = "Article Paragraph";
	public static final String GDPR_IS_MANDATORY_HEADER = "Mandatory";
	public static final String GDPR_IS_SYSTEM_HEADER = "System Question";
	public static final String GDPR_IS_ORGANIZATION_HEADER = "Organization Question";

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private DocumentsRepository documentsRepository;

	@Autowired
	private GDPRArticleChapterRepository gdprArticleChapterRepository;

	@Autowired
	private GDPRArticleChapterSectionRepository gdprArticleChapterSectionRepository;

	@Autowired
	private GDPRArticleItemRepository gdprArticleItemRepository;

	@Autowired
	private GDPRArticleModelDAO gdprArticleModelDAO;

	@Autowired
	private GDPRArticleParagraphRepository gdprArticleParagraphRepository;

	@Autowired
	private GDPRSystemArticleStatusLogModelDAO gdprSystemArticleStatusLogModelDAO;

	@Autowired
	private GDPRSystemArticleStatusModelDAO gdprSystemArticleStatusModelDAO;

	@Autowired
	private GDPRSystemArticleStatusRepository gdprSystemArticleStatusRepository;

	@Autowired
	private GDPRSystemStatusRepository gdprSystemStatusRepository;

	@Autowired
	private GDPRSystemArticleStatusLogRepository gdprSystemArticleStatusLogRepository;

	@Autowired
	private GDPRSystemStatusModelDAO gdprSystemStatusModelDAO;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private SystemsService systemsService;

	@Autowired
	private UserService userService;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Get GDPR Chapters list filtered
	 *
	 * @return items list
	 */
	public FilteredResponse<GDPRFilter, GDPRArticleChapterDTO> getGDPRArticleChapterListFiltered(FilteredRequest<GDPRFilter> filteredRequest) {
		List<GDPRArticleChapter> items = null;
		Long count = 0l;
		FilteredResponse<GDPRFilter, GDPRArticleChapterDTO> filteredResponse = new FilteredResponse<GDPRFilter, GDPRArticleChapterDTO>(filteredRequest);

		Organizations organization = organizationService.getCurrentOrganizationEntity();

		String namePattern = "";
		Long chapterNumber = null;
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
			if (NumberUtils.isCreatable(namePattern)) {
				chapterNumber = NumberUtils.createLong(namePattern);
			}
		}

		if (chapterNumber != null) {
			items = gdprArticleChapterRepository.getListByChapterNumber(chapterNumber, organization.getId(), filteredRequest.toPageRequest());
			count = gdprArticleChapterRepository.getCountByChapterNumber(chapterNumber, organization.getId());
		} else {
			items = gdprArticleChapterRepository.getListByName(namePattern, organization.getId(), filteredRequest.toPageRequest());
			count = gdprArticleChapterRepository.getCountByName(namePattern, organization.getId());
		}

		List<GDPRArticleChapterDTO> itemsDTOList = DTOBase.fromEntitiesList(items, GDPRArticleChapterDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get GDPR Chapter Sections list filtered
	 *
	 * @return items list
	 */
	public FilteredResponse<GDPRFilter, GDPRArticleChapterSectionDTO> getGDPRArticleChapterSectionListFiltered(FilteredRequest<GDPRFilter> filteredRequest) {
		List<GDPRArticleChapterSection> items = null;
		Long count = 0l;
		FilteredResponse<GDPRFilter, GDPRArticleChapterSectionDTO> filteredResponse = new FilteredResponse<GDPRFilter, GDPRArticleChapterSectionDTO>(filteredRequest);

		Organizations organization = organizationService.getCurrentOrganizationEntity();

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long chapterId = filteredRequest.getFilter().getChapterId();

		items = gdprArticleChapterSectionRepository.getListByChapterAndName(chapterId, namePattern, organization.getId(), filteredRequest.toPageRequest());
		count = gdprArticleChapterSectionRepository.getCountByChapterAndName(chapterId, namePattern, organization.getId());

		List<GDPRArticleChapterSectionDTO> itemsDTOList = DTOBase.fromEntitiesList(items, GDPRArticleChapterSectionDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get GDPR Articles list filtered
	 *
	 * @return items list
	 */
	public FilteredResponse<GDPRFilter, GDPRArticleItemDTO> getGDPRArticleItemsListFiltered(FilteredRequest<GDPRFilter> filteredRequest) {
		PagedResult<GDPRArticleItemDTO> result = gdprArticleModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<GDPRFilter, GDPRArticleItemDTO> filteredResponse = new FilteredResponse<GDPRFilter, GDPRArticleItemDTO>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Insert GDPR data from CSV file
	 */
	@Transactional
	public ImportResultDTO importFromCSVFile(MultipartFile file) {

		// Check Permissions to Import GDPR Data
		if (!userService.hasRole(RoleType.ADMIN) && !userService.hasRole(RoleType.ORGANIZATION_ADMIN)) {
			throw new ForbiddenException("You are not allowed to import GDPR data!", ApplicationExceptionCodes.GDPR_IMPORT_FORBIDDEN);
		}

		ImportResultDTO result = new ImportResultDTO();
		try {
			InputStream fileContentStream = file.getInputStream();
			result = importFromCSVFile(fileContentStream);
		} catch (IOException e) {
			log.error("Failed to import GDPR Items", e);
		}

		return result;
	}

	/**
	 * Insert GDPR data from CSV file
	 */
	@Transactional
	public ImportResultDTO importFromCSVFile(InputStream fileContentStream) {

		ImportResultDTO result = new ImportResultDTO();
		try {
			// Parse CSV file
			CSVParser csvParser = CSVUtils.createCSVParser(fileContentStream);
			List<CSVRecord> csvRecordList = csvParser.getRecords();
			Organizations organization = organizationService.getCurrentOrganizationEntity();
			log.info(MessageFormat.format("## Importing GDPR Data for Organization: {0}", organization.getName()));
			result = importFromCSVItems(csvRecordList, organization);
		} catch (IOException e) {
			log.error("Failed to import GDPR Items", e);
		}

		return result;
	}


	/**
	 * Insert some data from CSV file
	 */
	@Transactional
	protected ImportResultDTO importFromCSVItems(List<CSVRecord> csvRecordList, Organizations organization) {

		ImportResultDTO result = new ImportResultDTO();

		Map<String, GDPRArticleChapter> gdprArticleChapterMap = new HashMap<>();
		Map<String, GDPRArticleChapterSection> gdprArticleChapterSectionMap = new HashMap<>();
		Map<String, GDPRArticleItem> gdprArticleItemMap = new HashMap<>();
		Map<String, GDPRArticleParagraph> gdprArticleParagraphMap = new HashMap<>();

		// Proceed with user management
		for (CSVRecord csvRecord : csvRecordList) {

			// Accessing values by Header names
			String chapterRef = Optional.ofNullable(csvRecord.get(GDPR_CHAPTER_REF_HEADER)).orElse("").trim();
			String chapterName = Optional.ofNullable(csvRecord.get(GDPR_CHAPTER_NAME_HEADER)).orElse("").trim();
			String sectionRef = Optional.ofNullable(csvRecord.get(GDPR_SECTION_REF_HEADER)).orElse("").trim();
			String sectionName = Optional.ofNullable(csvRecord.get(GDPR_SECTION_NAME_HEADER)).orElse("").trim();
			String articleRef = Optional.ofNullable(csvRecord.get(GDPR_ARTICLE_REF_HEADER)).orElse("").trim();
			String articleName = Optional.ofNullable(csvRecord.get(GDPR_ARTICLE_NAME_HEADER)).orElse("").trim();
			String articleDescription = Optional.ofNullable(csvRecord.get(GDPR_ARTICLE_DESCRIPTION_HEADER)).orElse("").trim();
			String articleQuestion = Optional.ofNullable(csvRecord.get(GDPR_ARTICLE_DESCRIPTION_HEADER)).orElse("").trim();
			String articleBestPractice = Optional.ofNullable(csvRecord.get(GDPR_ARTICLE_BEST_PRACTICE_HEADER)).orElse("").trim();
			String articleParagraph = Optional.ofNullable(csvRecord.get(GDPR_PARAGRAPH_HEADER)).orElse("").trim();

			if (StringUtils.isNotEmpty(articleName)) {

				GDPRArticleChapter gdprArticleChapter = gdprArticleChapterMap.get(chapterRef);
				if (gdprArticleChapter == null) {
					Optional<GDPRArticleChapter> gdprArticleChapterOptional = gdprArticleChapterRepository.findFirstByNameIgnoreCaseAndOrganizationId(chapterName, organization.getId());
					if (!gdprArticleChapterOptional.isPresent()) {
						gdprArticleChapter = new GDPRArticleChapter();
						gdprArticleChapter.setOrganizationId(organization.getId());
						gdprArticleChapter.setName(chapterName);
						gdprArticleChapter.setReferenceNumber(chapterRef);
						try {
							gdprArticleChapter.setChapterNumber(Long.parseLong(chapterRef));
						} catch (NumberFormatException e) {
							;
						}
						gdprArticleChapter = gdprArticleChapterRepository.save(gdprArticleChapter);

						log.info(MessageFormat.format("## Create new GDPR Chapter [{0}, {1}]", chapterRef, chapterName));

						result.getCreated().add(new ItemViewDTO(gdprArticleChapter.getId(), gdprArticleChapter.getName()));
						result.getMessages().add(MessageFormat.format("Create new GDPR Chapter: {0}, {1}", chapterRef, chapterName));
					} else {
						gdprArticleChapter = gdprArticleChapterOptional.get();
					}

					// Put GDPR Item to the Map
					gdprArticleChapterMap.put(chapterRef, gdprArticleChapter);
				}

				// Process Section
				GDPRArticleChapterSection gdprArticleChapterSection = null;
				if (StringUtils.isNotEmpty(sectionName)) {
					gdprArticleChapterSection = gdprArticleChapterSectionMap.get(sectionName);
					if (gdprArticleChapterSection == null) {
						Optional<GDPRArticleChapterSection> gdprArticleChapterSectionOptional = gdprArticleChapterSectionRepository.findFirstByNameIgnoreCaseAndOrganizationId(sectionName, organization.getId());
						if (!gdprArticleChapterSectionOptional.isPresent()) {
							gdprArticleChapterSection = new GDPRArticleChapterSection();
							gdprArticleChapterSection.setOrganizationId(organization.getId());
							gdprArticleChapterSection.setName(sectionName);
							gdprArticleChapterSection.setReferenceNumber(sectionRef);
							gdprArticleChapterSection.setChapter(gdprArticleChapter);
							try {
								gdprArticleChapterSection.setSectionNumber(Long.parseLong(sectionRef));
							} catch (NumberFormatException e) {
								;
							}
							gdprArticleChapterSection = gdprArticleChapterSectionRepository.save(gdprArticleChapterSection);

							log.info(MessageFormat.format("## Create new GDPR Chapter Section [{0}, {1}]", sectionRef, sectionName));

							result.getCreated().add(new ItemViewDTO(gdprArticleChapterSection.getId(), gdprArticleChapterSection.getName()));
							result.getMessages().add(MessageFormat.format("Create new GDPR Chapter Section [{0}, {1}]", sectionRef, sectionName));
						} else {
							gdprArticleChapterSection = gdprArticleChapterSectionOptional.get();
						}

						// Put GDPR Item to the Map
						gdprArticleChapterSectionMap.put(sectionName, gdprArticleChapterSection);
					}
				}

				// Process Article
				GDPRArticleItem gdprArticleItem = null;
				gdprArticleItem = gdprArticleItemMap.get(articleRef);
				if (gdprArticleItem == null) {

					Long articleRefLong = null;
					try {
						articleRefLong = Long.parseLong(articleRef);
					} catch (NumberFormatException e) {
						;
					}

					Optional<GDPRArticleItem> gdprArticleItemOptional = gdprArticleItemRepository.findFirstByNameIgnoreCaseAndOrganizationId(articleName, organization.getId());
					if (!gdprArticleItemOptional.isPresent() && articleRefLong != null) {
						gdprArticleItemOptional = gdprArticleItemRepository.findFirstByArticleNumberAndOrganizationId(articleRefLong, organization.getId());
					}
					if (!gdprArticleItemOptional.isPresent()) {
						gdprArticleItem = new GDPRArticleItem();
						gdprArticleItem.setOrganizationId(organization.getId());
						gdprArticleItem.setName(articleName);
						gdprArticleItem.setReferenceNumber(articleRef);
						gdprArticleItem.setChapter(gdprArticleChapter);
						gdprArticleItem.setSection(gdprArticleChapterSection);
						gdprArticleItem.setArticleNumber(articleRefLong);
					} else {
						gdprArticleItem = gdprArticleItemOptional.get();
						gdprArticleItem.setName(articleName);
					}

					if (StringUtils.isNotEmpty(articleDescription)) gdprArticleItem.setDescription(articleDescription);
					if (StringUtils.isNotEmpty(articleQuestion)) gdprArticleItem.setQuestion(articleQuestion);
					if (StringUtils.isNotEmpty(articleBestPractice)) gdprArticleItem.setBestPractice(articleBestPractice);

					if (csvRecord.isMapped(GDPR_IS_MANDATORY_HEADER) && StringUtils.isNotEmpty(csvRecord.get(GDPR_IS_MANDATORY_HEADER))) {
						gdprArticleItem.setIsMandatory("yes".equalsIgnoreCase(csvRecord.get(GDPR_IS_MANDATORY_HEADER)));
					}
					if (csvRecord.isMapped(GDPR_IS_SYSTEM_HEADER) && StringUtils.isNotEmpty(csvRecord.get(GDPR_IS_SYSTEM_HEADER))) {
						gdprArticleItem.setIsSystemLevel("yes".equalsIgnoreCase(csvRecord.get(GDPR_IS_SYSTEM_HEADER)));
					}
					if (csvRecord.isMapped(GDPR_IS_ORGANIZATION_HEADER) && StringUtils.isNotEmpty(csvRecord.get(GDPR_IS_ORGANIZATION_HEADER))) {
						gdprArticleItem.setIsOrganizationLevel("yes".equalsIgnoreCase(csvRecord.get(GDPR_IS_ORGANIZATION_HEADER)));
					}

					boolean isNewItem = gdprArticleItem.getId() == null;
					gdprArticleItem = gdprArticleItemRepository.save(gdprArticleItem);

					if (isNewItem) {
						log.info(MessageFormat.format("## Create new GDPR Article [{0}, {1}]", chapterRef, chapterName));
						result.getCreated().add(new ItemViewDTO(gdprArticleItem.getId(), gdprArticleItem.getName()));
						result.getMessages().add(MessageFormat.format("Create new GDPR Article [{0}, {1}]", chapterRef, chapterName));
					} else {
						log.info(MessageFormat.format("## Update existing GDPR Article [{0}, {1}]", chapterRef, chapterName));
						result.getUpdated().add(new ItemViewDTO(gdprArticleItem.getId(), gdprArticleItem.getName()));
						result.getMessages().add(MessageFormat.format("Update existing GDPR Article [{0}, {1}]", chapterRef, chapterName));
					}

					// Put GDPR Item to the Map
					gdprArticleItemMap.put(articleRef, gdprArticleItem);
				}

				// Process Article
				GDPRArticleParagraph gdprArticleParagraph = null;
				if (StringUtils.isNotEmpty(articleParagraph)) {
					String paragraphIdString = articleRef + "/" + articleParagraph;
					gdprArticleParagraph = gdprArticleParagraphMap.get(paragraphIdString);
					if (gdprArticleParagraph == null) {
						Optional<GDPRArticleParagraph> gdprArticleParagraphOptional = gdprArticleParagraphRepository.findFirstByNameIgnoreCaseAndAndArticle(articleParagraph, gdprArticleItem);

						if (!gdprArticleParagraphOptional.isPresent()) {
							gdprArticleParagraph = new GDPRArticleParagraph();
							gdprArticleParagraph.setOrganizationId(organization.getId());
							gdprArticleParagraph.setName(articleParagraph);
							gdprArticleParagraph.setArticle(gdprArticleItem);
//							gdprArticleParagraph = gdprArticleParagraphRepository.save(gdprArticleParagraph);

							log.info(MessageFormat.format("## Create new GDPR Article Paragraph [{0}, {1}]", articleParagraph, articleName));
						} else {
							gdprArticleParagraph = gdprArticleParagraphOptional.get();
						}

						if (StringUtils.isNotEmpty(articleQuestion)) gdprArticleParagraph.setQuestion(articleQuestion);
						if (StringUtils.isNotEmpty(articleBestPractice)) gdprArticleParagraph.setBestPractice(articleBestPractice);
						if (csvRecord.isMapped(GDPR_IS_SYSTEM_HEADER) && StringUtils.isNotEmpty(csvRecord.get(GDPR_IS_SYSTEM_HEADER))) {
							gdprArticleParagraph.setIsSystemLevel("yes".equalsIgnoreCase(csvRecord.get(GDPR_IS_SYSTEM_HEADER)));
						}
						if (csvRecord.isMapped(GDPR_IS_ORGANIZATION_HEADER) && StringUtils.isNotEmpty(csvRecord.get(GDPR_IS_ORGANIZATION_HEADER))) {
							gdprArticleParagraph.setIsOrganizationLevel("yes".equalsIgnoreCase(csvRecord.get(GDPR_IS_ORGANIZATION_HEADER)));
						}

						gdprArticleParagraph = gdprArticleParagraphRepository.save(gdprArticleParagraph);
					}
				}

			}

		}

		return result;
	}

	/**
	 * Create CSV Printer to build GDPR Vocabulary
	 *
	 * @param outputStream
	 * @return
	 * @throws IOException
	 */
	private CSVPrinter createCsvPrinter(ByteArrayOutputStream outputStream) throws IOException {
		Writer writer = new OutputStreamWriter(outputStream);
		CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(
			GDPR_CHAPTER_REF_HEADER,
			GDPR_CHAPTER_NAME_HEADER,
			GDPR_SECTION_REF_HEADER,
			GDPR_SECTION_NAME_HEADER,
			GDPR_ARTICLE_REF_HEADER,
			GDPR_ARTICLE_NAME_HEADER,
			GDPR_ARTICLE_DESCRIPTION_HEADER,
			GDPR_PARAGRAPH_HEADER,
			GDPR_ARTICLE_BEST_PRACTICE_HEADER,
			GDPR_IS_MANDATORY_HEADER,
			GDPR_IS_SYSTEM_HEADER,
			GDPR_IS_ORGANIZATION_HEADER
		);
		return new CSVPrinter(writer, csvFormat);
	}

	/**
	 * Get content for Download
	 */
	public ByteArrayInputStream getDownloadData() {

		ByteArrayInputStream byteArrayInputStream = null;

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			CSVPrinter csvPrinter = createCsvPrinter(outputStream);
			Set<GDPRArticleItem> items = gdprArticleItemRepository.findAllByOrganizationIdOrderByArticleNumberAsc(organizationService.getCurrentOrganizationId());
			for (GDPRArticleItem item : items) {
				Set<GDPRArticleParagraph> paragraphs = item.getParagraphs();
				List<GDPRArticleParagraph> paragraphList = new ArrayList<>();
				paragraphList.add(null);
				paragraphList.addAll(paragraphs);

				for (GDPRArticleParagraph paragraph : paragraphList) {
					csvPrinter.printRecord(
						item.getChapter().getReferenceNumber(),
						item.getChapter().getName(),
						(item.getSection() != null ? item.getSection().getReferenceNumber() : ""),
						(item.getSection() != null ? item.getSection().getName() : ""),
						item.getReferenceNumber(),
						item.getName(),
						(paragraph != null ? paragraph.getQuestion() : item.getQuestion()),
						(paragraph != null ? paragraph.getName() : ""),
						(paragraph != null ? paragraph.getBestPractice() : item.getBestPractice()),
						(Boolean.TRUE.equals(item.getIsMandatory()) ? "YES" : "NO"),
						(Boolean.TRUE.equals(item.getIsSystemLevel()) ? "YES" : "NO"),
						(Boolean.TRUE.equals(item.getIsOrganizationLevel()) ? "YES" : "NO")
					);
				}
			}
			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			log.warn(e.getMessage(), e);
			throw new InternalServerErrorException("Failed to generate CSV Template file for GDPR Articles");
		}

		return byteArrayInputStream;
	}

}
