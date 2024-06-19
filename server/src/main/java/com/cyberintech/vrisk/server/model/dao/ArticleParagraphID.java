package com.cyberintech.vrisk.server.model.dao;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"articleId", "paragraphId"})
public class ArticleParagraphID {
	private Long articleId;
	private Long paragraphId;

	public ArticleParagraphID(Long articleId, Long paragraphId) {
		this.articleId = articleId;
		this.paragraphId = paragraphId;
	}
}
