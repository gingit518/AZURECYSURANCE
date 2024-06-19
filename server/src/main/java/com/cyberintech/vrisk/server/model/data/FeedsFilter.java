package com.cyberintech.vrisk.server.model.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Implementation of Feeds Filtering Logic
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2023-03-01
 */
@NoArgsConstructor
@Setter
@Getter
public class FeedsFilter extends NameFilter {
	private Boolean excludeExpired;
}
