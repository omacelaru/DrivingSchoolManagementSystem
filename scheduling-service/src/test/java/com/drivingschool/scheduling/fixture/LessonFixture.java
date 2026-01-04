package com.drivingschool.scheduling.fixture;

import com.drivingschool.scheduling.dto.LessonRequest;
import com.drivingschool.scheduling.dto.LessonResponse;
import com.drivingschool.scheduling.entity.Lesson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class LessonFixture {

    public static final LessonFixture INSTANCE = LessonFixture.builder()
            .defaultLessonId(1L)
            .defaultStudentId(1L)
            .defaultInstructorId(1L)
            .defaultCourseId(1L)
            .defaultStartTime(LocalDateTime.now().plusDays(1))
            .defaultEndTime(LocalDateTime.now().plusDays(1).plusHours(1).plusMinutes(30))
            .build();

    private final Long defaultLessonId;
    private final Long defaultStudentId;
    private final Long defaultInstructorId;
    private final Long defaultCourseId;
    private final LocalDateTime defaultStartTime;
    private final LocalDateTime defaultEndTime;

    public static Long defaultLessonId() {
        return INSTANCE.getDefaultLessonId();
    }

    public static Long defaultStudentId() {
        return INSTANCE.getDefaultStudentId();
    }

    public static Long defaultInstructorId() {
        return INSTANCE.getDefaultInstructorId();
    }

    public static Long defaultCourseId() {
        return INSTANCE.getDefaultCourseId();
    }

    public static LocalDateTime defaultStartTime() {
        return INSTANCE.getDefaultStartTime();
    }

    public static LocalDateTime defaultEndTime() {
        return INSTANCE.getDefaultEndTime();
    }

    public static LessonRequest lessonRequest() {
        return new LessonRequest(
                defaultStudentId(),
                defaultCourseId(),
                defaultStartTime(),
                defaultEndTime()
        );
    }

    public static LessonRequest lessonRequest(Long studentId, Long courseId, LocalDateTime startTime) {
        return new LessonRequest(
                studentId,
                courseId,
                startTime,
                startTime.plusHours(1).plusMinutes(30)
        );
    }

    public static LessonRequest lessonRequestWithoutEndTime() {
        return new LessonRequest(
                defaultStudentId(),
                defaultCourseId(),
                defaultStartTime(),
                null
        );
    }

    public static Lesson lesson() {
        return Lesson.builder()
                .id(defaultLessonId())
                .studentId(defaultStudentId())
                .course(CourseFixture.course())
                .startTime(defaultStartTime())
                .endTime(defaultEndTime())
                .status(Lesson.LessonStatus.SCHEDULED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Lesson lesson(Long id, Lesson.LessonStatus status) {
        return Lesson.builder()
                .id(id)
                .studentId(defaultStudentId())
                .course(CourseFixture.course())
                .startTime(defaultStartTime())
                .endTime(defaultEndTime())
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Lesson lessonScheduled() {
        return lesson(defaultLessonId(), Lesson.LessonStatus.SCHEDULED);
    }
}
