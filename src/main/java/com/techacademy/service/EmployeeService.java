package com.techacademy.service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.EmployeeRepository;
import com.techacademy.service.ReportService;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 従業員新規
    @Transactional
    public ErrorKinds save(Employee employee) {
        // パスワードチェック
        ErrorKinds result = employeePasswordCheck(employee);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        // 社員番号重複チェック
        if (findByCode(employee.getCode()) != null) {
            return ErrorKinds.DUPLICATE_ERROR;
        }

        employeeRepository.save(employee);
        return ErrorKinds.SUCCESS;
    }

    // 従業員更新
    @Transactional
    public ErrorKinds update(Employee employee) {
        // パスワードチェック
        if ("".equals(employee.getPassword())) {
            // パスワード入力ない場合は既存パスワードをセット
            employee.setPassword(findByCode(employee.getCode()).getPassword());
        } else {
            ErrorKinds result = employeePasswordCheck(employee);
            if (ErrorKinds.CHECK_OK != result) {
                return result;
            }
        }

        employee.setDeleteFlg(false);
        employeeRepository.save(employee);
        return ErrorKinds.SUCCESS;
    }

    // パスワードチェック
    private ErrorKinds employeePasswordCheck(Employee employee) {
        // パスワード半角英数字チェック
        if (isHalfSizeCheckError(employee)) {
            return ErrorKinds.HALFSIZE_ERROR;
        }

        // パスワード桁数チェック
        if (isOutOfRangePassword(employee)) {
            return ErrorKinds.RANGECHECK_ERROR;
        }

        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        return ErrorKinds.CHECK_OK;
    }

    // パスワード半角英数字チェック
    private boolean isHalfSizeCheckError(Employee employee) {
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
        Matcher matcher = pattern.matcher(employee.getPassword());
        return !matcher.matches();
    }

    // パスワード桁数チェック
    public boolean isOutOfRangePassword(Employee employee) {
        int passwordLength = employee.getPassword().length();
        return passwordLength < 8 || 16 < passwordLength;
    }

    @Autowired
    private ReportService reportService;

    // 従業員論理削除
    @Transactional
    public ErrorKinds delete(String code, UserDetail userDetail) {
        // ログイン中の従業員削除チェック
        if (code.equals(userDetail.getEmployee().getCode())) {
            return ErrorKinds.LOGINCHECK_ERROR;
        }

        Employee employee = findByCode(code);
        // 削除対象の従業員（employee）に紐づいている、日報のリスト（reportList）を取得
        List<Report> reportList = reportService.findByEmployee(employee);
        // 日報のリスト（reportList）を拡張for文を使って繰り返し
        for (Report report : reportList) {
            // 日報（report）のIDを指定して、日報情報を削除
            reportService.delete(report.getId());
        }

        employee.setDeleteFlg(true);
        return ErrorKinds.SUCCESS;
    }

    // 全件検索
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    // 1件検索
    public Employee findByCode(String code) {
        Optional<Employee> option = employeeRepository.findById(code);
        Employee employee = option.orElse(null);
        return employee;
    }



}
