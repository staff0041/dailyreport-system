package com.techacademy.controller;

import org.springframework.dao.DataIntegrityViolationException;
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
import com.techacademy.service.EmployeeService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("employees")

public class EmployeeController {
    private final EmployeeService employeeService;
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // 従業員一覧
    @GetMapping
    public String list(Model model) {
        model.addAttribute("employeeList", employeeService.findAll());
        model.addAttribute("listSize", employeeService.findAll().size());
        return "employees/list";
    }

    // 従業員情報詳細
    @GetMapping(value = "/{code}/")
    public String detail(@PathVariable String code, Model model) {
        model.addAttribute("employee", employeeService.findByCode(code));
        return "employees/detail";
    }

    // 従業員削除処理
    @PostMapping(value = "/{code}/delete")
    public String delete(@PathVariable String code, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        ErrorKinds result = employeeService.delete(code, userDetail);
        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("employee", employeeService.findByCode(code));
            return detail(code, model);
        }
        return "redirect:/employees";
    }

    // 従業員新規登録
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Employee employee) {
        return "employees/new";
    }

    // 従業員新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Employee employee, BindingResult res, Model model) {
    // public String add(@Validated @ModelAttribute Employee employee, BindingResult res, Model model) {
        // 入力チェックパスワード
        // 更新処理においてパスワード空白はエラーとしない仕様であるためエンティティ側で実装していない
        if ("".equals(employee.getPassword())) {
            // パスワード空白チェック
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.BLANK_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.BLANK_ERROR));
            return create(employee);
        }

        // 入力チェック
        if (res.hasErrors()) {
            return create(employee);
        }

        // カスタムエラーチェックと保存
        // 論理削除をした社員番号を指定すると例外になるためtry~catch(findByIdは削除フラグTRUEのデータ取得不可)
        try {
            ErrorKinds result = employeeService.save(employee);
            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                model.addAttribute("employee", employee);
                return create(employee);
            }
        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create(employee);
        }
        return "redirect:/employees";
    }

    // 従業員更新
    @GetMapping(value = "/{code}/update")
    public String edit(@PathVariable String code, Model model) {
        if (code != null) {
            model.addAttribute("employee", employeeService.findByCode(code));
        }
        return "employees/update";
    }

    // 従業員情報更新処理
    @PostMapping(value = "/{code}/update")
    public String update(@PathVariable String code, @Validated Employee employee, BindingResult res, Model model) {
        // 入力チェック
        if (res.hasErrors()) {
            code = null;
            return edit(code, model);
        }

        // カスタムエラーチェックと更新
        ErrorKinds result = employeeService.update(employee);
        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            code = null;
            return edit(code, model);
        }

        return "redirect:/employees";
    }

}
