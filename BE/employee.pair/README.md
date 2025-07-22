# Employee Pair Analysis API

A Spring Boot REST API application that analyzes employee work periods on projects to find pairs of employees who have worked together for the longest time.

## 🚀 Features

- **CSV File Upload**: Upload employee project data via REST API
- **Pair Analysis**: Find the pair of employees who worked together the longest
- **Data Validation**: Comprehensive validation of CSV data with detailed error messages
- **Exception Handling**: Centralized error handling with proper HTTP status codes
- **In-Memory Database**: Uses H2 database for fast data processing
- **Cross-Origin Support**: CORS enabled for frontend integration

## 🛠️ Technology Stack

- **Java 17**
- **Spring Boot 3.5.3**
- **Spring Data JPA**
- **Spring Web**
- **Spring Validation**
- **H2 Database** (in-memory)
- **Lombok** (for reducing boilerplate code)
- **Maven** (dependency management)

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+ (or use the included Maven wrapper)

## 🏃‍♂️ Getting Started

### 1. Clone the Repository
```bash
git clone <repository-url>
cd employee.pair
```

### 2. Build the Application
```bash
# Using Maven wrapper (recommended)
./mvnw clean install

# Or using system Maven
mvn clean install
```

### 3. Run the Application
```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or using system Maven
mvn spring-boot:run

# Or run the JAR file
java -jar target/employee.pair-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## 📊 CSV Data Format

The application expects CSV files with the following format:
```
EmpID, ProjectID, DateFrom, DateTo
143, 12, 2013-11-01, 2014-01-05
218, 10, 2012-05-16, NULL
143, 10, 2009-01-01, 2011-04-27
```

### Data Rules:
- **EmpID**: Employee identifier (integer)
- **ProjectID**: Project identifier (integer)  
- **DateFrom**: Start date (YYYY-MM-DD format)
- **DateTo**: End date (YYYY-MM-DD format, or NULL for current date)

## 🔌 API Endpoints

### Upload CSV File
```http
POST /api/employees/upload
Content-Type: multipart/form-data

Parameters:
- file: CSV file containing employee project data
```

**Response:**
```json
{
  "message": "File uploaded and processed successfully",
  "filename": "employees.csv"
}
```

### Get Longest Working Pair
```http
GET /api/employees/longest-pair
```

**Response:**
```json
{
  "employee1Id": 143,
  "employee2Id": 218,
  "projectId": 10,
  "totalDays": 365,
  "commonProjects": [
    {
      "projectId": 10,
      "daysWorkedTogether": 365,
      "overlapStart": "2012-05-16",
      "overlapEnd": "2013-05-15"
    }
  ]
}
```

## 🏗️ Project Structure

```
src/main/java/com/sirma/task/employee/pair/
├── Application.java                 # Main Spring Boot application
├── controller/
│   └── EmployeeController.java      # REST endpoints
├── service/
│   └── EmployeeService.java         # Business logic and CSV processing
├── model/
│   └── Employee.java                # JPA entity
├── repository/
│   └── EmployeeRepository.java      # Data access layer
├── exception/                       # Custom exceptions and global handler
│   ├── DataNotFoundException.java
│   ├── FileProcessingException.java
│   ├── ValidationException.java
│   └── GlobalExceptionHandler.java
└── dto/
    └── ErrorResponse.java           # Error response structure
```

## 🔧 Configuration

The application uses the following default configuration (`application.properties`):

```properties
spring.application.name=employee.pair
server.port=8080
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
```

### H2 Database Console
Access the H2 database console at: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: `password`

## 🚨 Error Handling

The application provides comprehensive error handling for various scenarios:

- **File Processing Errors**: Invalid CSV format, parsing errors
- **Validation Errors**: Missing required fields, invalid data types
- **Data Not Found**: No employees or pairs found
- **File Size Limits**: Exceeded upload size limits
- **General Errors**: Unexpected server errors

All errors return structured JSON responses with appropriate HTTP status codes.

## 🧪 Testing

Run the test suite:
```bash
./mvnw test
```


## 📄 License

This project is part of a coding task for Sirma.