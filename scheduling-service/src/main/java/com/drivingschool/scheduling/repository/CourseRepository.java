package com.drivingschool.scheduling.repository;

import com.drivingschool.scheduling.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByInstructorId(Long instructorId);
    boolean existsByVehicleId(Long vehicleId);

    List<Course> findByInstructorId(Long instructorId);
    List<Course> findByVehicleId(Long vehicleId);
    List<Course> findByInstructorIdAndVehicleId(Long instructorId, Long vehicleId);
}

