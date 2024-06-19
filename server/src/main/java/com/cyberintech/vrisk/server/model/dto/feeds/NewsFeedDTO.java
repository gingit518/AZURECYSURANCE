package com.cyberintech.vrisk.server.model.dto.feeds;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.News;
import com.cyberintech.vrisk.server.model.jpa.entity.UserMessages;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;

/**
 * News Feed data object
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  2.0.14
 * @since    2022-11-28
 */
/*@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)*/
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class NewsFeedDTO extends DTOBase<News> {

	@Schema
	private Long id;

	private String uid;

	@Schema
	private String title;

	@Schema
	private String description;

	@Schema
	private String url;

	@Schema
	private Date date;

	@Schema
	private Date expiryDate;

	public NewsFeedDTO(News entity) {
		super(entity);
	}
	@Override
	public void fromEntity(News entity) {
		id = entity.getId();
		uid = entity.getUid();
		title = entity.getName();
		description = entity.getDescription();
		url = entity.getUrl();
		date = entity.getDate();
		expiryDate = entity.getExpiryDate();
	}
	@JsonIgnore
	@Hidden
	public String getUid() {
		return uid;
	}
}
