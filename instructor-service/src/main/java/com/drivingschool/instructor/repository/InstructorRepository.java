package com.drivingschool.instructor.repository;

import com.drivingschool.instructor.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findByLicenseNumber(String licenseNumber);
    Optional<Instructor> findByEmail(String email);
    List<Instructor> findBySpecialization(Instructor.Specialization specialization);
}

