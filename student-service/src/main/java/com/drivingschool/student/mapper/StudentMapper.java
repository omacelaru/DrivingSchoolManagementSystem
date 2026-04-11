package com.drivingschool.student.mapper;

import com.drivingschool.student.dto.DocumentResponse;
import com.drivingschool.student.dto.StudentProfileResponse;
import com.drivingschool.student.dto.StudentRequest;
import com.drivingschool.student.dto.StudentResponse;
import com.drivingschool.student.entity.Document;
import com.drivingschool.student.entity.DrivingLicenseCategory;
import com.drivingschool.student.entity.Student;
import com.drivingschool.student.entity.StudentProfile;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "targetLicenseCategories", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @BeanMapping(ignoreUnmappedSourceProperties = {"profile", "targetDrivingCategoryCodes"})
    Student toEntity(StudentRequest request);

    @Mapping(target = "documents", source = "documents", qualifiedByName = "documentList")
    @Mapping(target = "profile", source = "profile")
    @Mapping(target = "targetDrivingCategoryCodes", source = "targetLicenseCategories", qualifiedByName = "licenseCategoriesToCodes")
    StudentResponse toResponse(Student student);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "targetLicenseCategories", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"profile", "targetDrivingCategoryCodes"})
    void updateEntity(@MappingTarget Student student, StudentRequest request);

    DocumentResponse toDocumentResponse(Document document);

    StudentProfileResponse toStudentProfileResponse(StudentProfile profile);

    @Named("documentList")
    default List<DocumentResponse> documentList(List<Document> docs) {
        if (docs == null) {
            return null;
        }
        return docs.stream().map(this::toDocumentResponse).toList();
    }

    @Named("licenseCategoriesToCodes")
    default List<String> licenseCategoriesToCodes(Set<DrivingLicenseCategory> categories) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }
        return categories.stream().map(Enum::name).sorted().toList();
    }
}
