package com.drivingschool.scheduling.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Course name is required")
    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Instructor ID is required")
    @Column(name = "instructor_id", nullable = false)
    private Long instructorId;

    @NotNull(message = "Vehicle ID is required")
    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @NotNull(message = "Number of lessons is required")
    @Positive(message = "Number of lessons must be positive")
    @Column(name = "number_of_lessons", nullable = false)
    private Integer numberOfLessons;

    @NotNull(message = "Course type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", nullable = false)
    private CourseType courseType;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    //todo investigate jsonignore
//    @JsonIgnore
    private List<Lesson> lessons = new ArrayList<>();

    @Version
    private Long version;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    public enum CourseType {
        THEORETICAL, PRACTICAL
    }

    /**
     * Calculates the total duration of the course in hours based on all lessons.
     * Sums up the duration of each lesson (endTime - startTime).
     * 
     * @return Total duration in hours (as integer, rounded)
     */
    public Integer getDuration() {
        if (lessons == null || lessons.isEmpty()) {
            return 0;
        }
        
        long totalMinutes = lessons.stream()
                .filter(lesson -> lesson.getStartTime() != null && lesson.getEndTime() != null)
                .mapToLong(lesson -> {
                    Duration duration = Duration.between(lesson.getStartTime(), lesson.getEndTime());
                    return duration.toMinutes();
                })
                .sum();
        
        // Convert minutes to hours (rounded)
        return (int) Math.round(totalMinutes / 60.0);
    }

    /**
     * Gets the number of lessons booked for a specific student in this course.
     * 
     * @param studentId The student ID to count lessons for
     * @return Number of lessons booked by the student in this course
     */
    public long getBookedLessonsCountForStudent(Long studentId) {
        if (lessons == null || lessons.isEmpty()) {
            return 0;
        }
        return lessons.stream()
                .filter(lesson -> lesson.getStudentId().equals(studentId))
                .count();
    }

    /**
     * Calculates the price per lesson for this course.
     * 
     * @return Price per lesson (course price / number of lessons)
     */
    public BigDecimal getPricePerLesson() {
        if (numberOfLessons == null || numberOfLessons == 0) {
            return BigDecimal.ZERO;
        }
        return price.divide(BigDecimal.valueOf(numberOfLessons), 2, java.math.RoundingMode.HALF_UP);
    }
}

