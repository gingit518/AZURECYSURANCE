package com.cyberintech.vrisk.server.model.data;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * Base Filter class
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-04
 */
@Getter
@Setter
public abstract class BaseFilter<T> implements Serializable {

	private static final long serialVersionUID = -4477981817676574215L;

	private List<T> excludeIds;
}
