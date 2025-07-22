package com.sirma.task.employee.pair.repository;

import com.sirma.task.employee.pair.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e ORDER BY e.empId, e.projectId")
    List<Employee> findAllOrderedByEmployeeAndProject();
}
