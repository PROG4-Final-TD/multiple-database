package com.example.prog4.controller.view;

import com.example.prog4.controller.PopulateController;
import com.example.prog4.controller.mapper.EmployeeMapper;
import com.example.prog4.model.Employee;
import com.example.prog4.model.EmployeeFilter;
import com.example.prog4.service.EmployeeService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/employee")
@AllArgsConstructor
public class EmployeeViewController extends PopulateController {
    private EmployeeService employeeService;
    private EmployeeMapper employeeMapper;

    @GetMapping("/list")
    public String getAll(
            @ModelAttribute EmployeeFilter filters,
            Model model,
            HttpSession session
    ) {
        model.addAttribute("employees", employeeService.getAll(filters).stream().map(employeeMapper::toView).toList())
                .addAttribute("employeeFilters", filters)
                .addAttribute("directions", Sort.Direction.values());
        session.setAttribute("employeeFiltersSession", filters);

        return "employees";
    }

    @GetMapping("/create")
    public String createEmployee(Model model) {
        model.addAttribute("employee", Employee.builder().build());
        return "employee_creation";
    }

    @GetMapping("/edit/{eId}")
    public String editEmployee(@PathVariable String eId, Model model) {
        Employee toEdit = employeeMapper.toView(employeeService.getOne(eId));
        String cnaps = employeeService.getEmployeeCnaps(eId);
        model.addAttribute("employee", toEdit);
        model.addAttribute("cnapsNumber", cnaps);

        return "employee_edition";
    }

    @GetMapping("/show/{eId}")
    public String showEmployee(@PathVariable String eId, Model model) {
        Employee toShow = employeeMapper.toView(employeeService.getOne(eId));
        String cnaps = employeeService.getEmployeeCnaps(eId);
        model.addAttribute("employee", toShow);
        model.addAttribute("cnaps", cnaps);

        return "employee_show";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/employee/list";
    }
}
