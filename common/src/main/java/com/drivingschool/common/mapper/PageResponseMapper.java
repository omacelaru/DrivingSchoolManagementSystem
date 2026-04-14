package com.drivingschool.common.mapper;

import com.drivingschool.common.dto.PageResponse;
import org.springframework.data.domain.Page;

public final class PageResponseMapper {

    private PageResponseMapper() {
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.getSort().toString()
        );
    }
}

