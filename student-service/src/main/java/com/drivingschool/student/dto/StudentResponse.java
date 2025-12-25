package com.drivingschool.student.dto;

import com.drivingschool.student.entity.Student;

import java.time.LocalDateTime;
import java.util.List;

public class StudentResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String cnp;
    private String email;
    private String phone;
    private String address;
    private Student.StudentStatus status;
    private LocalDateTime registrationDate;
    private LocalDateTime lastModifiedDate;
    private List<DocumentResponse> documents;

    public StudentResponse() {
    }

    public StudentResponse(Long id, String firstName, String lastName, String cnp, String email, String phone, String address, Student.StudentStatus status, LocalDateTime registrationDate, LocalDateTime lastModifiedDate, List<DocumentResponse> documents) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.cnp = cnp;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.status = status;
        this.registrationDate = registrationDate;
        this.lastModifiedDate = lastModifiedDate;
        this.documents = documents;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCnp() {
        return cnp;
    }

    public void setCnp(String cnp) {
        this.cnp = cnp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Student.StudentStatus getStatus() {
        return status;
    }

    public void setStatus(Student.StudentStatus status) {
        this.status = status;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public List<DocumentResponse> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentResponse> documents) {
        this.documents = documents;
    }
}
