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

/**
 * Category inclusion rules limited to categories currently modeled in this project enum.
 * Selecting a key category should implicitly include the listed categories.
 */
export const DRIVING_CATEGORY_INCLUDES: Record<DrivingLicenseCategoryCode, DrivingLicenseCategoryCode[]> = {
  AM: [],
  A1: ["AM"],
  A2: ["A1", "AM"],
  A: ["A2", "A1", "AM"],
  B: ["AM"],
  BE: ["B", "AM"],
  C: ["B", "AM"],
  CE: ["C", "BE", "B", "AM"],
  D: ["B", "AM"]
};

export function expandDrivingCategories(
  selected: Iterable<DrivingLicenseCategoryCode>
): Set<DrivingLicenseCategoryCode> {
  const expanded = new Set<DrivingLicenseCategoryCode>();
  const visit = (code: DrivingLicenseCategoryCode): void => {
    if (expanded.has(code)) return;
    expanded.add(code);
    for (const included of DRIVING_CATEGORY_INCLUDES[code]) {
      visit(included);
    }
  };

  for (const code of selected) {
    visit(code);
  }
  return expanded;
}
