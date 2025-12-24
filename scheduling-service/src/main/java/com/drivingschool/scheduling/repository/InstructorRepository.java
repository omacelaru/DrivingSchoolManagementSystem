package com.drivingschool.scheduling.repository;

import com.drivingschool.scheduling.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findByLicenseNumber(String licenseNumber);
    Optional<Instructor> findByEmail(String email);
    List<Instructor> findBySpecialization(Instructor.Specialization specialization);
    
    @Query("SELECT i FROM Instructor i WHERE i.id NOT IN " +
           "(SELECT DISTINCT l.instructor.id FROM Lesson l WHERE " +
           "l.status = 'SCHEDULED' AND " +
           "((l.startTime <= :startTime AND l.endTime > :startTime) OR " +
           "(l.startTime < :endTime AND l.endTime >= :endTime) OR " +
           "(l.startTime >= :startTime AND l.endTime <= :endTime)))")
    List<Instructor> findAvailableInstructors(@Param("startTime") LocalDateTime startTime, 
                                              @Param("endTime") LocalDateTime endTime);
}

