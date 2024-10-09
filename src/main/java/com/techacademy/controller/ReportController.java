package com.techacademy.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.service.EmployeeService;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")

public class ReportController {
    private final ReportService reportService;
    @SuppressWarnings("unused")
    private final EmployeeService employeeService;
    public ReportController(ReportService reportService, EmployeeService employeeService) {
        this.reportService = reportService;
        this.employeeService = employeeService;
    }


   // 日報一覧
   @GetMapping
   public String list(@AuthenticationPrincipal UserDetail userDetail, Model model) {
       List<Report> reportList;

       // ユーザーの権限を確認
       if (userDetail.getEmployee().getRole() == Employee.Role.GENERAL) {
           // 一般ユーザーの場合、その社員番号の日報のみを取得
           reportList = reportService.findByEmployee(userDetail.getEmployee());
       } else {
           // 管理者の場合、全ての日報を取得
           reportList = reportService.findAll();
       }

       model.addAttribute("reportList", reportList);
       model.addAttribute("listSize", reportList.size());
       return "reports/list";
   }


    // 日報詳細
    @GetMapping(value = "/{id}/")
    public String detail(@PathVariable int id, Model model) {
        model.addAttribute("report", reportService.findById(id));
        return "reports/detail";
    }


    // 日報削除処理
    @PostMapping(value = "/{id}/delete")
    public String delete(@PathVariable int id, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        ErrorKinds result = reportService.delete(id);
        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("report", reportService.findById(id));
            return detail(id, model);
        }
        return "redirect:/reports";
    }


    // 日報新規登録
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Report report, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        model.addAttribute("name", userDetail.getEmployee().getName());
        return "reports/new";
    }

    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Report report, BindingResult res, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        model.addAttribute("name", userDetail.getEmployee().getName());

        // 入力チェック
        if (res.hasErrors()) {
            return create(report, userDetail, model);
        }

        // カスタムエラーチェックと保存
        report.setEmployee(userDetail.getEmployee());
        ErrorKinds result = reportService.save(report);
        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("report", report);
            return create(report, userDetail, model);
        }

        return "redirect:/reports";
    }

    // 日報更新
    @GetMapping(value = "/{id}/update")
    public String edit(@PathVariable Integer id, Model model) {
        if (id != null) {
            model.addAttribute("report", reportService.findById(id));
        }
        return "reports/update";
    }

    // 日報更新処理
    @PostMapping(value = "/{id}/update")
    public String update(@PathVariable Integer id, @Validated Report report, BindingResult res, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        // 入力チェック
        if (res.hasErrors()) {
            id = null;
            return edit(id, model);
        }

        // カスタムエラーチェックと更新
        report.setEmployee(userDetail.getEmployee());
        ErrorKinds result = reportService.update(report);
        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            id = null;
            return edit(id, model);
        }

        return "redirect:/reports";
    }

}
