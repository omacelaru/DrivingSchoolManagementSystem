package com.drivingschool.vehicle.pagination;

import com.drivingschool.common.pagination.SortField;

public enum VehicleSortField implements SortField {
    LICENSE_PLATE("licensePlate"),
    MAKE("make"),
    MODEL("model"),
    YEAR("year"),
    CREATED_AT("createdAt", true);

    private final String property;
    private final boolean defaultField;

    VehicleSortField(String property) {
        this(property, false);
    }

    VehicleSortField(String property, boolean defaultField) {
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

