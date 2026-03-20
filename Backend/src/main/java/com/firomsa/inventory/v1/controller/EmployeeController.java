package com.firomsa.inventory.v1.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employee")
@Tag(name = "Employee", description = "API for employee operations")
@Slf4j
public class EmployeeController {

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public String employeeHome() {
        return "Employee area is accessible";
    }
}
