package com.firomsa.inventory.v1.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/employee")
@Tag(name = "Employee", description = "API for employee operations")
@Slf4j
public class EmployeeController {

}
