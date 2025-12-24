package com.drivingschool.payment.repository;

import com.drivingschool.payment.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByCategory(Course.CourseCategory category);
}

