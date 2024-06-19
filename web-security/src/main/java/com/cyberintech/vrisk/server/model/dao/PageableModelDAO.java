package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseFilter;
import com.cyberintech.vrisk.server.model.data.BaseSort;
import org.springframework.data.domain.Pageable;

public interface PageableModelDAO<ENTITY, F extends BaseFilter> {

	PagedResult<ENTITY> getItemsPageable(F filter, Pageable pageable, BaseSort sort);

}
