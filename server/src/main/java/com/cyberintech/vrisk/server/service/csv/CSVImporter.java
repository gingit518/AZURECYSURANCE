package com.cyberintech.vrisk.server.service.csv;

import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;

import java.io.InputStream;

/**
 * @author Andrii Iakovenko
 * @since  2022-07-19
 */
public interface CSVImporter {

	ImportResultDTO doImport(InputStream inputStream);

}
