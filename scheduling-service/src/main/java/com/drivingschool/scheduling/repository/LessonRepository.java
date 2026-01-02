package com.drivingschool.scheduling.repository;

import com.drivingschool.scheduling.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByStudentId(Long studentId);
    List<Lesson> findByInstructorId(Long instructorId);
    List<Lesson> findByInstructorIdAndStatus(Long instructorId, Lesson.LessonStatus status);
    
    @Query("SELECT l FROM Lesson l WHERE l.instructorId = :instructorId AND " +
           "l.status = 'SCHEDULED' AND " +
           "((l.startTime <= :startTime AND l.endTime > :startTime) OR " +
           "(l.startTime < :endTime AND l.endTime >= :endTime) OR " +
           "(l.startTime >= :startTime AND l.endTime <= :endTime))")
    List<Lesson> findConflictingLessons(@Param("instructorId") Long instructorId,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);
}

