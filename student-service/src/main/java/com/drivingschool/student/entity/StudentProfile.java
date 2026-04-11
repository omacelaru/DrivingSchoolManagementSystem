package com.drivingschool.student.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "student_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    @Size(max = 100)
    @Column(length = 100)
    private String emergencyContactName;

    @Size(max = 10)
    @Column(length = 10)
    private String emergencyContactPhone;

    @Size(max = 2000)
    @Column(length = 2000)
    private String notes;
}
