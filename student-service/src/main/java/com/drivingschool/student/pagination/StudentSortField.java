package com.drivingschool.student.pagination;

import com.drivingschool.common.pagination.SortField;

public enum StudentSortField implements SortField {
    FIRST_NAME("firstName"),
    LAST_NAME("lastName"),
    EMAIL("email"),
    REGISTRATION_DATE("registrationDate", true);

    private final String property;
    private final boolean defaultField;

    StudentSortField(String property) {
        this(property, false);
    }

    StudentSortField(String property, boolean defaultField) {
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

