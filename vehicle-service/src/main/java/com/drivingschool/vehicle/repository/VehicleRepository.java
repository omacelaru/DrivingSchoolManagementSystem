package com.drivingschool.vehicle.repository;

import com.drivingschool.vehicle.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    List<Vehicle> findByStatus(Vehicle.VehicleStatus status);
    Page<Vehicle> findByStatus(Vehicle.VehicleStatus status, Pageable pageable);
}

