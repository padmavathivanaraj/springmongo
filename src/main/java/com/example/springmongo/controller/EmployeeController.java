package com.example.springmongo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.springmongo.model.Employee;
import com.example.springmongo.repository.EmployeeRepository;

@Controller
public class EmployeeController {

    private final EmployeeRepository repo;

    public EmployeeController(EmployeeRepository repo) {
        this.repo = repo;
    }

    // Landing page with form
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("employee", new Employee());
        return "index";
    }

    // Receive form submission (from index.html) and save, then redirect to displayAll
    @PostMapping("/employees")
    public String saveEmployeeFromForm(@ModelAttribute Employee employee, Model model) {
        if (employee.getEmployeeId() != null && repo.existsByEmployeeId(employee.getEmployeeId())) {
            model.addAttribute("error", "Employee ID already exists");
            model.addAttribute("employee", employee);
            return "index";
        }
        repo.save(employee);
        return "redirect:/displayAll";
    }

    // Show all employees in an HTML page
    @GetMapping("/displayAll")
    public String displayAll(Model model) {
        List<Employee> list = repo.findAll();
        model.addAttribute("employees", list);
        return "displayAll";
    }

    // Show one employee detail page by employeeId (business id)
    @GetMapping("/display/{employeeId}")
    public String displayByEmployeeId(@PathVariable String employeeId, Model model) {
        Optional<Employee> opt = repo.findByEmployeeId(employeeId);
        if (opt.isPresent()) {
            model.addAttribute("employee", opt.get());
        } else {
            model.addAttribute("message", "Employee not found with id: " + employeeId);
        }
        return "employeeDetail";
    }

    /* --- REST APIs (JSON) --- */

    // Get all employees
    @GetMapping("/api/employees")
    @ResponseBody
    public List<Employee> apiGetAll() {
        return repo.findAll();
    }

    // Get by MongoDB _id
    @GetMapping("/api/employees/id/{id}")
    @ResponseBody
    public ResponseEntity<Employee> getEmployeeByMongoId(@PathVariable String id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/api/employees/{employeeId}")
@ResponseBody
public ResponseEntity<Employee> getEmployeeByBusinessId(@PathVariable String employeeId) {
    return repo.findByEmployeeId(employeeId)
               .map(ResponseEntity::ok)
               .orElseGet(() -> ResponseEntity.notFound().build());
}

    // Create new employee
    @PostMapping("/api/employees")
    @ResponseBody
    public ResponseEntity<?> apiCreate(@RequestBody Employee emp) {
        if (emp.getEmployeeId() == null || emp.getEmployeeId().isBlank()) {
            return ResponseEntity.badRequest().body("employeeId is required");
        }
        if (repo.existsByEmployeeId(emp.getEmployeeId())) {
            return ResponseEntity.status(409).body("employeeId already exists");
        }
        Employee saved = repo.save(emp);
        return ResponseEntity.ok(saved);
    }
}
