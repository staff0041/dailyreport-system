package com.techacademy.repository;

import java.time.LocalDate;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Integer> {
       boolean existsByEmployeeCodeAndReportDate(String employeeCode, LocalDate reportDate);
       List<Report> findByEmployee(Employee employee);
}
