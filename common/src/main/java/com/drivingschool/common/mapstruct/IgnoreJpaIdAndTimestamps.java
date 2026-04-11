package com.drivingschool.common.mapstruct;

import org.mapstruct.Mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Shared MapStruct ignores for entities with {@code id}, {@code createdAt}, {@code lastModifiedDate}
 * (Spring Data JPA auditing). See MapStruct "Mapping composition" (meta-annotations).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
@Mapping(target = "id", ignore = true)
@Mapping(target = "createdAt", ignore = true)
@Mapping(target = "lastModifiedDate", ignore = true)
public @interface IgnoreJpaIdAndTimestamps {
}
