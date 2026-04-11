package com.drivingschool.scheduling.mapper;

import com.drivingschool.common.mapstruct.IgnoreJpaIdAndTimestamps;
import com.drivingschool.scheduling.dto.CourseRequest;
import com.drivingschool.scheduling.dto.CourseResponse;
import com.drivingschool.scheduling.entity.Course;
import com.drivingschool.scheduling.entity.CourseTag;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @IgnoreJpaIdAndTimestamps
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "courseTags", ignore = true)
    @Mapping(target = "version", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = "courseTagCodes")
    Course toEntity(CourseRequest request);

    @Mapping(target = "courseTagCodes", source = "courseTags", qualifiedByName = "tagsToCodes")
    CourseResponse toResponse(Course course);

    @IgnoreJpaIdAndTimestamps
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "courseTags", ignore = true)
    @Mapping(target = "version", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = "courseTagCodes")
    void updateEntity(@MappingTarget Course course, CourseRequest request);

    @Named("tagsToCodes")
    default List<String> tagsToCodes(Set<CourseTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream().map(CourseTag::getCode).sorted().toList();
    }
}
