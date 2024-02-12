package com.example.payroll.controllers;

import com.example.payroll.models.Employee;
import com.example.payroll.repositories.EmployeeRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;


//Note: The data being testing was preloaded in com.example.payroll.repositories.EmployeeRepository

@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAllEmployees() throws Exception {
        int size = repository.findAll().size();

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(size)));
    }

    @Test
    public void testGetOneEmployee() throws Exception {
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Smith"))
                .andExpect(jsonPath("$.role").value("CTO"))
                .andExpect(jsonPath("$.salary").value(500000.00));
    }

    @Test
    public void testEmployeeNotFound() throws Exception {
        mockMvc.perform(get("/employees/55"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Could not find employee 55"));
    }

    @Test
    public void testCreateEmployee() throws Exception {
        Employee employeeDetails = new Employee("Jane Smith", "COO", 900000.00);
        String jsonRequest = objectMapper.writeValueAsString(employeeDetails);

        ResultActions resultActions = mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        );

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(employeeDetails.getName()))
                .andExpect(jsonPath("$.role").value(employeeDetails.getRole()))
                .andExpect(jsonPath("$.salary").value(employeeDetails.getSalary()));
    }

    @Test
    public void testEditEmployee() throws Exception {

        Employee newEmployee = new Employee("Jane Smith", "COO", 500000);
        repository.save(newEmployee);

        double newSalary = 345000.00;
        newEmployee.setSalary(newSalary);

        String jsonRequest = objectMapper.writeValueAsString(newEmployee);

        ResultActions resultActions = mockMvc.perform(put("/employees/" + newEmployee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        );

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(newEmployee.getId()))
                .andExpect(jsonPath("$.name").value(newEmployee.getName()))
                .andExpect(jsonPath("$.role").value(newEmployee.getRole()))
                .andExpect(jsonPath("$.salary").value(newSalary));

        Employee updatedEmployee = repository.findById(newEmployee.getId()).orElse(null);

        assert updatedEmployee != null;
        assertEquals(newEmployee.getName(), updatedEmployee.getName());
        assertEquals(newEmployee.getRole(), updatedEmployee.getRole());
        assertEquals(newSalary, updatedEmployee.getSalary());
    }

    @Test
    public void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/employees/1")).andExpect(status().isOk());
        assertNull(repository.findById(1L).orElse(null));
    }
}
