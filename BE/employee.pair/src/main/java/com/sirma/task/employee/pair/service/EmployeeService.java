package com.sirma.task.employee.pair.service;

import com.sirma.task.employee.pair.exception.DataNotFoundException;
import com.sirma.task.employee.pair.exception.FileProcessingException;
import com.sirma.task.employee.pair.exception.ValidationException;
import com.sirma.task.employee.pair.model.Employee;
import com.sirma.task.employee.pair.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public void processCSVFile(MultipartFile file) {
        validateFile(file);
        
        List<Employee> employees = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                if (StringUtils.hasText(line)) {
                    Employee employee = parseCSVLine(line, lineNumber);
                    if (employee != null) {
                        employees.add(employee);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error reading CSV file: {}", e.getMessage(), e);
            throw new FileProcessingException("Failed to read CSV file: " + e.getMessage(), e);
        }

        if (employees.isEmpty()) {
            throw new ValidationException("No valid employee records found in the CSV file");
        }

        try {
            employeeRepository.deleteAll();
            employeeRepository.saveAll(employees);
            log.info("Successfully processed {} employee records", employees.size());
        } catch (Exception e) {
            log.error("Error saving employee data: {}", e.getMessage(), e);
            throw new FileProcessingException("Failed to save employee data: " + e.getMessage(), e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null) {
            throw new ValidationException("File cannot be null");
        }
        
        if (file.isEmpty()) {
            throw new ValidationException("File cannot be empty");
        }
        
        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename) || !filename.toLowerCase().endsWith(".csv")) {
            throw new ValidationException("File must be a CSV file");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new ValidationException("File size cannot exceed 10MB");
        }
    }

    private Integer parseInteger(String value, String fieldName, int lineNumber) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ValidationException(String.format("Line %d: Invalid %s '%s'", lineNumber, fieldName, value));
        }
    }

    public Map<String, Object> findLongestWorkingPair() {
        List<Employee> allEmployees = employeeRepository.findAllOrderedByEmployeeAndProject();
        
        if (allEmployees.isEmpty()) {
            throw new DataNotFoundException("No employee data found. Please upload a CSV file first.");
        }

        Map<String, Long> pairDays = new HashMap<>();
        Map<String, List<Map<String, Object>>> pairProjects = new HashMap<>();

        Map<Integer, List<Employee>> projectGroups = allEmployees.stream()
                .collect(Collectors.groupingBy(Employee::getProjectId));

        for (Map.Entry<Integer, List<Employee>> entry : projectGroups.entrySet()) {
            List<Employee> projectEmployees = entry.getValue();

            for (int i = 0; i < projectEmployees.size(); i++) {
                for (int j = i + 1; j < projectEmployees.size(); j++) {
                    Employee emp1 = projectEmployees.get(i);
                    Employee emp2 = projectEmployees.get(j);

                    long overlapDays = calculateOverlapDays(emp1, emp2);
                    if (overlapDays > 0) {
                        String pairKey = createPairKey(emp1.getEmpId(), emp2.getEmpId());

                        pairDays.put(pairKey, pairDays.getOrDefault(pairKey, 0L) + overlapDays);

                        pairProjects.computeIfAbsent(pairKey, k -> new ArrayList<>()).add(
                                Map.of(
                                        "empId1", emp1.getEmpId(),
                                        "empId2", emp2.getEmpId(),
                                        "projectId", emp1.getProjectId(),
                                        "daysWorked", overlapDays,
                                        "dateFrom", emp1.getDateFrom().isAfter(emp2.getDateFrom()) ? emp1.getDateFrom() : emp2.getDateFrom(),
                                        "dateTo", emp1.getDateTo().isBefore(emp2.getDateTo()) ? emp1.getDateTo() : emp2.getDateTo()
                                )
                        );
                    }
                }
            }
        }
        
        if (pairDays.isEmpty()) {
            throw new DataNotFoundException("No employee pairs found that worked together on the same projects.");
        }

        String maxPair = pairDays.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");

        Long maxDays = pairDays.getOrDefault(maxPair, 0L);
        List<Map<String, Object>> projects = pairProjects.getOrDefault(maxPair, new ArrayList<>());

        return Map.of(
                "pair", maxPair,
                "totalDays", maxDays,
                "projects", projects
        );
    }

    private String createPairKey(Integer empId1, Integer empId2) {
        if (empId1 <= empId2) {
            return empId1 + "," + empId2;
        } else {
            return empId2 + "," + empId1;
        }
    }

    private long calculateOverlapDays(Employee emp1, Employee emp2) {
        LocalDate start1 = emp1.getDateFrom();
        LocalDate end1 = emp1.getDateTo() != null ? emp1.getDateTo() : LocalDate.now();
        LocalDate start2 = emp2.getDateFrom();
        LocalDate end2 = emp2.getDateTo() != null ? emp2.getDateTo() : LocalDate.now();

        LocalDate overlapStart = start1.isAfter(start2) ? start1 : start2;
        LocalDate overlapEnd = end1.isBefore(end2) ? end1 : end2;

        if (overlapStart.isBefore(overlapEnd) || overlapStart.equals(overlapEnd)) {
            return overlapStart.until(overlapEnd).getDays() + 1;
        }

        return 0;
    }

    private LocalDate parseDate(String dateStr, int lineNumber) {
        if (dateStr == null || dateStr.trim().isEmpty() || "NULL".equalsIgnoreCase(dateStr.trim())) {
            return null;
        }

        for (DateTimeFormatter formatter : dateFormatters) {
            try {
                return LocalDate.parse(dateStr.trim(), formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new ValidationException(String.format("Line %d: Unable to parse date '%s'", lineNumber, dateStr));
    }

    private Employee parseCSVLine(String line, int lineNumber) {
        try {
            String[] parts = line.split(",");
            if (parts.length < 4) {
                return null;
            }

            Integer empId = parseInteger(parts[0].trim(), "Employee ID", lineNumber);
            Integer projectId = parseInteger(parts[1].trim(), "Project ID", lineNumber);
            LocalDate dateFrom = parseDate(parts[2].trim(), lineNumber);
            LocalDate dateTo = parseDate(parts[3].trim(), lineNumber);

            if (dateTo == null) {
                dateTo = LocalDate.now();
            }

            if (dateFrom.isAfter(dateTo)) {
                throw new ValidationException(String.format("Line %d: Start date cannot be after end date", lineNumber));
            }

            return new Employee(empId, projectId, dateFrom, dateTo);
        } catch (Exception e) {
            if (e instanceof ValidationException) {
                throw e;
            }
            throw new ValidationException(String.format("Line %d: Error parsing line - %s", lineNumber, e.getMessage()));
        }
    }

    private final List<DateTimeFormatter> dateFormatters = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );
}