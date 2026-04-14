package com.drivingschool.instructor.mapper;

import com.drivingschool.common.mapstruct.IgnoreJpaIdAndTimestamps;
import com.drivingschool.instructor.dto.InstructorRequest;
import com.drivingschool.instructor.dto.InstructorResponse;
import com.drivingschool.instructor.entity.Instructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InstructorMapper {

    @IgnoreJpaIdAndTimestamps
    @Mapping(target = "rating", constant = "0.0")
    Instructor toEntity(InstructorRequest request);

    @IgnoreJpaIdAndTimestamps
    @Mapping(target = "rating", ignore = true)
    void updateEntity(@MappingTarget Instructor instructor, InstructorRequest request);

    InstructorResponse toResponse(Instructor instructor);
}
