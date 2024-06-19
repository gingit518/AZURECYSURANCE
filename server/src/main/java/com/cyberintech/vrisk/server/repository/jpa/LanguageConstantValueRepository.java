package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.LanguageConstantValues;
import com.cyberintech.vrisk.server.model.jpa.entity.SupportedLanguages;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LanguageConstantValueRepository extends CoreRepository<LanguageConstantValues, Long> {

	Optional<LanguageConstantValues> findById(Long itemId);

	Optional<LanguageConstantValues> findFirstByLanguageIdAndLanguageConstantId(Long languageId, Long languageConstantId);

	@Query("SELECT new com.cyberintech.vrisk.server.model.jpa.entity.LanguageConstantValues(lc, lcv) FROM LanguageConstants lc " +
		"LEFT JOIN FETCH LanguageConstantValues lcv ON lcv.languageConstant = lc WHERE lc.name IN :constantCodes and lcv.language.code NOT IN :languageCodes ORDER BY lc.name ASC")
	List<LanguageConstantValues> getListByConstantCodes(@Param("constantCodes") Collection<String> constantCodes, @Param("languageCodes") Collection<String> languageCodes);

	@Query("SELECT new com.cyberintech.vrisk.server.model.jpa.entity.LanguageConstantValues(lc, lcv) FROM LanguageConstants lc " +
		"LEFT JOIN FETCH LanguageConstantValues lcv ON lcv.languageConstant = lc AND lcv.language = :language ORDER BY lc.name ASC")
	List<LanguageConstantValues> getListByLanguageForAllConstants(@Param("language") SupportedLanguages language);

	List<LanguageConstantValues> getListByLanguage(SupportedLanguages language);

}
