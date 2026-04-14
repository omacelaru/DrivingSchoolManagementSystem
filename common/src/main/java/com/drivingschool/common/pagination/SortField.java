package com.drivingschool.common.pagination;

public interface SortField {
    String property();

    default boolean isDefault() {
        return false;
    }
}

