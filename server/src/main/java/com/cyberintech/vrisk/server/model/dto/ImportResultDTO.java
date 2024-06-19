package com.cyberintech.vrisk.server.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Result of file import
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-04-12
 */
@Setter
@Getter
public class ImportResultDTO implements Serializable {

	private static final long serialVersionUID = 6223505590943578051L;

	@Schema
	private List<String> messages;

	@Schema
	private String status;

	private List<ItemViewDTO> created;

	private List<ItemViewDTO> updated;

	private List<ItemViewDTO> ignored;

	public ImportResultDTO() {
		messages = new ArrayList<>();
		created = new ArrayList<>();
		updated = new ArrayList<>();
		ignored = new ArrayList<>();
	}

	/**
	 * Load all info from another import result
	 *
	 * @param initial
	 */
	public void load(ImportResultDTO initial) {
		messages.addAll(initial.getMessages());
		created.addAll(initial.getCreated());
		updated.addAll(initial.getUpdated());
		ignored.addAll(initial.getIgnored());
	}

	public String getSummaryInfo() {
		return String.format("Status: %s, Created: %s, Updated: %s, Ignored: %s", status, created.size(), updated.size(), ignored.size());
	}

}
