package com.drivingschool.student.repository;

import com.drivingschool.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByCnp(String cnp);
    Optional<Student> findByEmail(String email);
    List<Student> findByStatus(Student.StudentStatus status);
    
    @Query("SELECT s FROM Student s WHERE s.firstName LIKE %:name% OR s.lastName LIKE %:name%")
    List<Student> findByNameContaining(@Param("name") String name);
    
    boolean existsByCnp(String cnp);
    boolean existsByEmail(String email);
}

