# Employee Pair Finder

This Angular application identifies pairs of employees who have worked together on common projects for the longest period of time.

## Features

- CSV file upload for employee project data
- Support for multiple date formats
- Automatic calculation of the longest working employee pair
- Display of all common projects with days worked
- Responsive design

## Requirements

- Node.js v12 or higher
- Angular 17

## Project Structure

```
employee-pair-finder/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── file-upload/       # File upload component
│   │   │   └── employee-pairs/    # Results display component
│   │   ├── models/                # Data models
│   │   ├── services/              
│   │   │   ├── csv.service.ts     # CSV parsing and local processing
│   │   │   └── api.service.ts     # Back-end API integration
│   │   ├── app.component.*        # Main app component
│   │   └── app.module.ts          # Main app module
│   ├── assets/                    # Static assets
│   │   ├── sample.csv             # Sample CSV file
│   ├── environments/              # Environment configurations
│   ├── index.html                 # Main HTML file
│   ├── main.ts                    # Main entry point
│   └── styles.scss                # Global styles
├── angular.json                   # Angular configuration
├── package.json                   # Dependencies
├── tsconfig.json                  # TypeScript configuration
├── LICENSE                        # MIT License
├── build.bat                      # Build script
└── start.bat                      # Start script
```

## CSV Format

The application expects a CSV file with the following columns:
- EmpID: Employee ID (number)
- ProjectID: Project ID (number)
- DateFrom: Start date (supports multiple formats)
- DateTo: End date (supports multiple formats, can be NULL for current date)

Example:
```
EmpID, ProjectID, DateFrom, DateTo
143, 12, 2013-11-01, 2014-01-05
218, 10, 2012-05-16, NULL
143, 10, 2009-01-01, 2011-04-27
```

## Getting Started

### Quick Start
1. Clone the repository
2. Run the `start.bat` script to install dependencies and start the development server
3. Open your browser to `http://localhost:4200`

### Manual Setup
1. Clone the repository
2. Install dependencies: `npm install`
3. Run the development server: `npm start`
4. Open your browser to `http://localhost:4200`

## How It Works

1. Upload a CSV file with employee project data
2. The application parses the CSV and identifies employee pairs who worked on the same projects
3. For each pair, it calculates the overlapping time periods on each project
4. The pair with the longest total overlapping time is displayed along with all their common projects

## Supported Date Formats

The application supports multiple date formats including:
- ISO format (YYYY-MM-DD)
- US format (MM/DD/YYYY)
- European format (DD/MM/YYYY)
- And various other common formats with different separators
