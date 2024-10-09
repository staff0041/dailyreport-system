package com.techacademy.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.repository.EmployeeRepository;
import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    @SuppressWarnings("unused")
    private final EmployeeRepository employeeRepository;
    public ReportService(ReportRepository reportRepository, EmployeeRepository employeeRepository) {
        this.reportRepository = reportRepository;
        this.employeeRepository = employeeRepository;
    }

    // 日報新規
    @Transactional
    public ErrorKinds save(Report report) {
        // 社員番号と日付重複チェック
        String employeeCode = report.getEmployee().getCode();
        LocalDate reportDate = report.getReportDate();
        if (reportRepository.existsByEmployeeCodeAndReportDate(employeeCode, reportDate)) {
            return ErrorKinds.DATECHECK_ERROR;
        }

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // 日報更新
    @Transactional
    public ErrorKinds update(Report report) {
        // 日付の変更チェック
        Report existingReport = reportRepository.findById(report.getId()).orElse(null);
        if (!existingReport.getReportDate().equals(report.getReportDate())) {
            // 社員番号と日付重複チェック
            String employeeCode = report.getEmployee().getCode();
            LocalDate reportDate = report.getReportDate();
            if (reportRepository.existsByEmployeeCodeAndReportDate(employeeCode, reportDate)) {
                return ErrorKinds.DATECHECK_ERROR;
            }
        }

        report.setDeleteFlg(false);
        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }


    // 日報論理削除
    @Transactional
    // public ErrorKinds delete(Integer id, UserDetail userDetail) {
    public ErrorKinds delete(Integer id) {
        Report report = findById(id);
        report.setDeleteFlg(true);
        return ErrorKinds.SUCCESS;
    }


    // 全件検索
    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    // 1件検索
    public Report findById(Integer id) {
        Optional<Report> option = reportRepository.findById(id);
        Report report = option.orElse(null);
        return report;
    }

    // // 1件検索
    // public Report findByReportDate(LocalDate date) {
    //     Optional<Report> option = reportRepository.findByReportDate(date);
    //     Report report = option.orElse(null);
    //     return report;
    // }

    // Employee に紐づくレポートを取得するメソッド
    public List<Report> findByEmployee(Employee employee) {
        return reportRepository.findByEmployee(employee);
    }

}
