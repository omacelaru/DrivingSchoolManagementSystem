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
    @Query("SELECT l FROM Lesson l WHERE l.course.instructorId = :instructorId")
    List<Lesson> findByInstructorId(@Param("instructorId") Long instructorId);
    
    @Query("SELECT l FROM Lesson l WHERE l.course.instructorId = :instructorId AND l.status = :status")
    List<Lesson> findByInstructorIdAndStatus(@Param("instructorId") Long instructorId, 
                                            @Param("status") Lesson.LessonStatus status);
    
    @Query("SELECT l FROM Lesson l WHERE l.course.id = :courseId")
    List<Lesson> findByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT l FROM Lesson l WHERE l.studentId = :studentId AND l.status = :status")
    List<Lesson> findByStudentIdAndStatus(@Param("studentId") Long studentId, 
                                          @Param("status") Lesson.LessonStatus status);
    
    @Query("SELECT l FROM Lesson l WHERE l.startTime >= :startTime AND l.endTime <= :endTime")
    List<Lesson> findByDateRange(@Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT l FROM Lesson l WHERE l.studentId = :studentId AND l.startTime >= :fromDate")
    List<Lesson> findUpcomingByStudentId(@Param("studentId") Long studentId,
                                         @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT l FROM Lesson l WHERE l.course.instructorId = :instructorId AND " +
           "l.status = 'SCHEDULED' AND " +
           "((l.startTime <= :startTime AND l.endTime > :startTime) OR " +
           "(l.startTime < :endTime AND l.endTime >= :endTime) OR " +
           "(l.startTime >= :startTime AND l.endTime <= :endTime))")
    List<Lesson> findConflictingLessons(@Param("instructorId") Long instructorId,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT l FROM Lesson l WHERE l.course.vehicleId = :vehicleId AND " +
           "l.status = 'SCHEDULED' AND " +
           "((l.startTime <= :startTime AND l.endTime > :startTime) OR " +
           "(l.startTime < :endTime AND l.endTime >= :endTime) OR " +
           "(l.startTime >= :startTime AND l.endTime <= :endTime))")
    List<Lesson> findConflictingLessonsForVehicle(@Param("vehicleId") Long vehicleId,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);
}

