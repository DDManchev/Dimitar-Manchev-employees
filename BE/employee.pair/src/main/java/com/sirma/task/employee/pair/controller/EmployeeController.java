package com.sirma.task.employee.pair.controller;

import com.sirma.task.employee.pair.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadCSV(@RequestParam("file") MultipartFile file) {
        log.info("Received file upload request: {}", file.getOriginalFilename());
        
        employeeService.processCSVFile(file);
        
        return ResponseEntity.ok(Map.of(
                "message", "File uploaded and processed successfully",
                "filename", file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown"
        ));
    }

    @GetMapping("/longest-pair")
    public ResponseEntity<Map<String, Object>> getLongestWorkingPair() {
        log.info("Received request for longest working pair analysis");
        
        Map<String, Object> result = employeeService.findLongestWorkingPair();
        
        return ResponseEntity.ok(result);
    }
}
