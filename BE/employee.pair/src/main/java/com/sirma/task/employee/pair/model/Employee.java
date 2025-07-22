package com.sirma.task.employee.pair.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entity representing an employee's work period on a project
 */
@Entity
@Table(name = "employee_projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emp_id", nullable = false)
    @NotNull(message = "Employee ID cannot be null")
    private Integer empId;

    @Column(name = "project_id", nullable = false)
    @NotNull(message = "Project ID cannot be null")
    private Integer projectId;

    @Column(name = "date_from", nullable = false)
    @NotNull(message = "Start date cannot be null")
    private LocalDate dateFrom;

    @Column(name = "date_to")
    private LocalDate dateTo;

    /**
     * Constructor without ID (for creating new entities)
     */
    public Employee(Integer empId, Integer projectId, LocalDate dateFrom, LocalDate dateTo) {
        this.empId = empId;
        this.projectId = projectId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }
}
