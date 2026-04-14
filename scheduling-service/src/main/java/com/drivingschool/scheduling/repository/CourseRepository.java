package com.drivingschool.scheduling.repository;

import com.drivingschool.scheduling.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByInstructorId(Long instructorId);

    boolean existsByVehicleId(Long vehicleId);

    Page<Course> findByInstructorId(Long instructorId, Pageable pageable);

    Page<Course> findByVehicleId(Long vehicleId, Pageable pageable);

    Page<Course> findByInstructorIdAndVehicleId(Long instructorId, Long vehicleId, Pageable pageable);
}

