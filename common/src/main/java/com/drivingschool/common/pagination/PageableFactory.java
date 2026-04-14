package com.drivingschool.common.pagination;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class PageableFactory {

    private PageableFactory() {
    }

    public static <E extends Enum<E> & SortField> Pageable build(
            Integer page,
            Integer size,
            String sortBy,
            String sortDir,
            int defaultPageSize,
            Class<E> sortEnumType
    ) {
        int validatedPage = page != null && page >= 0 ? page : 0;
        int validatedSize = size != null && size > 0 ? size : defaultPageSize;

        E resolvedField = resolveSortField(sortBy, sortEnumType);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return PageRequest.of(validatedPage, validatedSize, Sort.by(direction, resolvedField.property()));
    }

    private static <E extends Enum<E> & SortField> E resolveSortField(String sortBy, Class<E> sortEnumType) {
        E[] values = sortEnumType.getEnumConstants();
        String candidate = sortBy == null || sortBy.isBlank()
                ? Arrays.stream(values).filter(SortField::isDefault).findFirst().map(SortField::property).orElse(values[0].property())
                : sortBy;

        return Arrays.stream(values)
                .filter(v -> v.property().equals(candidate))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "Unsupported sort field: " + candidate + ". Allowed fields: " +
                                Arrays.stream(values).map(SortField::property).collect(Collectors.joining(", ")),
                        ErrorCode.BUSINESS_ERROR
                ));
    }
}

