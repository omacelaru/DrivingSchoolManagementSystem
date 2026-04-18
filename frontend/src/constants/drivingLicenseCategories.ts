/**
 * Mirrors {@code com.drivingschool.student.entity.DrivingLicenseCategory} (student-service).
 * EU/RO-style categories; API expects uppercase codes.
 */
export const DRIVING_LICENSE_CATEGORY_CODES = ["AM", "A1", "A2", "A", "B", "BE", "C", "CE", "D"] as const;

export type DrivingLicenseCategoryCode = (typeof DRIVING_LICENSE_CATEGORY_CODES)[number];

/** Short hints for the registration UI (not shown to the API). */
export const LICENSE_CATEGORY_HINTS: Record<DrivingLicenseCategoryCode, string> = {
  AM: "Mopeds & light quadricycles",
  A1: "Light motorcycles (up to 125 cm³)",
  A2: "Medium motorcycles",
  A: "Heavy motorcycles",
  B: "Cars (standard passenger vehicle)",
  BE: "Car + trailer combinations",
  C: "Heavy goods vehicles",
  CE: "Articulated lorries (C + trailer)",
  D: "Buses & coaches"
};
