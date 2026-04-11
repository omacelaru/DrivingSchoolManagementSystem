package com.drivingschool.scheduling.fixture;

import com.drivingschool.scheduling.dto.CourseRequest;
import com.drivingschool.scheduling.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CourseFixture {

    public static final CourseFixture INSTANCE = CourseFixture.builder()
            .defaultCourseId(1L)
            .defaultName("Beginner Course")
            .defaultDescription("Complete beginner course with 10 practical lessons")
            .defaultPrice(new BigDecimal("1000.00"))
            .defaultInstructorId(1L)
            .defaultVehicleId(1L)
            .defaultNumberOfLessons(10)
            .defaultCourseType(Course.CourseType.PRACTICAL)
            .build();

    private final Long defaultCourseId;
    private final String defaultName;
    private final String defaultDescription;
    private final BigDecimal defaultPrice;
    private final Long defaultInstructorId;
    private final Long defaultVehicleId;
    private final Integer defaultNumberOfLessons;
    private final Course.CourseType defaultCourseType;

    public static Long defaultCourseId() {
        return INSTANCE.getDefaultCourseId();
    }

    public static String defaultName() {
        return INSTANCE.getDefaultName();
    }

    public static String defaultDescription() {
        return INSTANCE.getDefaultDescription();
    }

    public static BigDecimal defaultPrice() {
        return INSTANCE.getDefaultPrice();
    }

    public static Long defaultInstructorId() {
        return INSTANCE.getDefaultInstructorId();
    }

    public static Long defaultVehicleId() {
        return INSTANCE.getDefaultVehicleId();
    }

    public static Integer defaultNumberOfLessons() {
        return INSTANCE.getDefaultNumberOfLessons();
    }

    public static Course.CourseType defaultCourseType() {
        return INSTANCE.getDefaultCourseType();
    }

    public static CourseRequest courseRequest() {
        return new CourseRequest(
                defaultName(),
                defaultDescription(),
                defaultPrice(),
                defaultInstructorId(),
                defaultVehicleId(),
                defaultNumberOfLessons(),
                defaultCourseType(),
                null
        );
    }

    public static CourseRequest courseRequest(Long instructorId, Long vehicleId) {
        return new CourseRequest(
                defaultName(),
                defaultDescription(),
                defaultPrice(),
                instructorId,
                vehicleId,
                defaultNumberOfLessons(),
                defaultCourseType(),
                null
        );
    }

    public static Course course() {
        return Course.builder()
                .id(defaultCourseId())
                .name(defaultName())
                .description(defaultDescription())
                .price(defaultPrice())
                .instructorId(defaultInstructorId())
                .vehicleId(defaultVehicleId())
                .numberOfLessons(defaultNumberOfLessons())
                .courseType(defaultCourseType())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
