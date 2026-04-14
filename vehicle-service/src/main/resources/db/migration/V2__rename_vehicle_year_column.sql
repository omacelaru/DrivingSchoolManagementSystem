-- Align schema with entity mapping: Vehicle.year -> manufacture_year
ALTER TABLE vehicles RENAME COLUMN year TO manufacture_year;
