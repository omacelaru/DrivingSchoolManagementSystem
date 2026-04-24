package com.drivingschool.instructor.pagination;

import com.drivingschool.common.pagination.SortField;

public enum InstructorSortField implements SortField {
    FIRST_NAME("firstName"),
    LAST_NAME("lastName"),
    EMAIL("email"),
    CREATED_AT("createdAt", true);

    private final String property;
    private final boolean defaultField;

    InstructorSortField(String property) {
        this(property, false);
    }

    InstructorSortField(String property, boolean defaultField) {
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
