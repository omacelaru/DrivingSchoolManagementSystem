package com.drivingschool.scheduling.repository;

import com.drivingschool.scheduling.entity.CourseTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CourseTagRepository extends JpaRepository<CourseTag, Long> {

    List<CourseTag> findByCodeIn(Collection<String> codes);
}
