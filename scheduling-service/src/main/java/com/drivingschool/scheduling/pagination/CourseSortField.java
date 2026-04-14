package com.drivingschool.scheduling.pagination;

import com.drivingschool.common.pagination.SortField;

public enum CourseSortField implements SortField {
    NAME("name"),
    PRICE("price"),
    TOTAL_LESSONS("totalLessons"),
    CREATED_AT("createdAt", true);

    private final String property;
    private final boolean defaultField;

    CourseSortField(String property) {
        this(property, false);
    }

    CourseSortField(String property, boolean defaultField) {
        this.property = property;
        this.defaultField = defaultField;
    }

    @Override
    public String property() {
        return property;
    }

    @Override
    public boolean isDefault() {
        return defaultField;
    }
}

