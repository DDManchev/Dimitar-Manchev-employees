export interface EmployeeRecord {
  empId: number;
  projectId: number;
  dateFrom: Date;
  dateTo: Date | null;
}

export interface EmployeePair {
  employee1Id: number;
  employee2Id: number;
  projectId: number;
  daysWorked: number;
}

export interface LongestPair {
  employee1Id: number;
  employee2Id: number;
  totalDaysWorked: number;
  projects: EmployeePair[];
}
