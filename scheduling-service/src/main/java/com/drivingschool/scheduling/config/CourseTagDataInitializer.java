package com.drivingschool.scheduling.config;

import com.drivingschool.scheduling.entity.CourseTag;
import com.drivingschool.scheduling.repository.CourseTagRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CourseTagDataInitializer implements ApplicationRunner {

    private final CourseTagRepository courseTagRepository;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        if (courseTagRepository.count() > 0) {
            return;
        }
        courseTagRepository.saveAll(List.of(
                CourseTag.builder().code("INTENSIVE").name("Intensive programme").build(),
                CourseTag.builder().code("EVENING").name("Evening slots").build(),
                CourseTag.builder().code("WEEKEND").name("Weekend slots").build(),
                CourseTag.builder().code("EXAM_PREP").name("Exam preparation").build()
        ));
    }
}
