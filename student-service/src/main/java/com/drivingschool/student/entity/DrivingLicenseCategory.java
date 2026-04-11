package com.drivingschool.student.entity;

import java.util.Locale;

/**
 * EU/RO-style driving licence categories. Fixed in law — enum keeps the model simple;
 * values are still stored in table {@code student_target_license_categories} via {@code @ElementCollection}.
 */
public enum DrivingLicenseCategory {
    AM,
    A1,
    A2,
    A,
    B,
    BE,
    C,
    CE,
    D;

    public static DrivingLicenseCategory fromApiCode(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Driving license category code must not be blank");
        }
        return DrivingLicenseCategory.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }
}
