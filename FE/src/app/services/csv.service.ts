import { Injectable } from "@angular/core";
import { BehaviorSubject } from "rxjs";
import * as Papa from "papaparse";
import * as moment from "moment";
import {
  EmployeeRecord,
  EmployeePair,
  LongestPair,
} from "../models/employee.model";

@Injectable({
  providedIn: "root",
})
export class CsvService {
  private employeePairsSubject = new BehaviorSubject<LongestPair | null>(null);
  employeePairs$ = this.employeePairsSubject.asObservable();

  constructor() {}

  parseCSV(file: File): Promise<void> {
    return new Promise((resolve, reject) => {
      if (file.size === 0) {
        reject(
          new Error(
            "The uploaded file is empty. Please upload a valid CSV file."
          )
        );
        return;
      }

      Papa.parse(file, {
        header: false,
        skipEmptyLines: true,
        complete: (results) => {
          try {
            if (results.data.length <= 1) {
              reject(
                new Error(
                  "The CSV file contains no data rows. Please upload a file with employee records."
                )
              );
              return;
            }

            const records = this.processData(results.data);

            if (records.length === 0) {
              reject(
                new Error(
                  "No valid employee records found in the CSV file. Please check the format."
                )
              );
              return;
            }

            const pairs = this.findLongestWorkingPair(records);

            if (!pairs) {
              reject(
                new Error(
                  "No overlapping work periods found between employees. Please check your data."
                )
              );
              return;
            }

            this.employeePairsSubject.next(pairs);
            resolve();
          } catch (error: any) {
            reject(
              new Error(
                `Error processing CSV data: ${error.message || "Unknown error"}`
              )
            );
          }
        },
        error: (error) => {
          reject(
            new Error(
              `Error parsing CSV file: ${error.message || "Unknown error"}`
            )
          );
        },
      });
    });
  }

  private processData(data: any[]): EmployeeRecord[] {
    const records: EmployeeRecord[] = [];

    const startIndex = this.hasHeader(data) ? 1 : 0;

    for (let i = startIndex; i < data.length; i++) {
      const row = data[i];
      if (row.length < 3) continue;

      const empId = parseInt(row[0].trim(), 10);
      const projectId = parseInt(row[1].trim(), 10);
      const dateFrom = this.parseDate(row[2].trim());

      let dateTo = null;
      if (row[3] && row[3].trim().toUpperCase() !== "NULL") {
        dateTo = this.parseDate(row[3].trim());
      }

      if (!isNaN(empId) && !isNaN(projectId) && dateFrom) {
        records.push({
          empId,
          projectId,
          dateFrom,
          dateTo,
        });
      }
    }

    return records;
  }

  private hasHeader(data: any[]): boolean {
    if (data.length === 0) return false;

    const firstRow = data[0];
    if (firstRow.length < 2) return false;

    return isNaN(parseInt(firstRow[0], 10)) || isNaN(parseInt(firstRow[1], 10));
  }

  private parseDate(dateStr: string): Date | null {
    const formats = [
      "YYYY-MM-DD",
      "MM/DD/YYYY",
      "DD/MM/YYYY",
      "DD-MM-YYYY",
      "MM-DD-YYYY",
      "YYYY/MM/DD",
      "DD.MM.YYYY",
      "MM.DD.YYYY",
    ];

    const momentDate = moment(dateStr, formats, true);
    return momentDate.isValid() ? momentDate.toDate() : null;
  }

  private findLongestWorkingPair(
    records: EmployeeRecord[]
  ): LongestPair | null {
    const today = new Date();
    const projectPairs: Map<string, EmployeePair[]> = new Map();

    for (let i = 0; i < records.length; i++) {
      for (let j = i + 1; j < records.length; j++) {
        const emp1 = records[i];
        const emp2 = records[j];

        if (emp1.empId === emp2.empId || emp1.projectId !== emp2.projectId) {
          continue;
        }

        const startDate = new Date(
          Math.max(emp1.dateFrom.getTime(), emp2.dateFrom.getTime())
        );

        const emp1EndDate = emp1.dateTo || today;
        const emp2EndDate = emp2.dateTo || today;

        const endDate = new Date(
          Math.min(emp1EndDate.getTime(), emp2EndDate.getTime())
        );

        if (startDate <= endDate) {
          const daysWorked = Math.ceil(
            (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24)
          );

          const emp1Id = Math.min(emp1.empId, emp2.empId);
          const emp2Id = Math.max(emp1.empId, emp2.empId);
          const pairKey = `${emp1Id}-${emp2Id}`;

          if (!projectPairs.has(pairKey)) {
            projectPairs.set(pairKey, []);
          }

          projectPairs.get(pairKey)!.push({
            employee1Id: emp1Id,
            employee2Id: emp2Id,
            projectId: emp1.projectId,
            daysWorked,
          });
        }
      }
    }

    let maxDays = 0;
    let longestPair: LongestPair | null = null;

    projectPairs.forEach((pairs, key) => {
      const totalDays = pairs.reduce((sum, pair) => sum + pair.daysWorked, 0);

      if (totalDays > maxDays) {
        maxDays = totalDays;
        const [emp1Id, emp2Id] = key.split("-").map((id) => parseInt(id, 10));

        longestPair = {
          employee1Id: emp1Id,
          employee2Id: emp2Id,
          totalDaysWorked: totalDays,
          projects: pairs,
        };
      }
    });

    return longestPair;
  }
}
