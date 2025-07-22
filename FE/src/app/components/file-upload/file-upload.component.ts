import { Component, ElementRef, ViewChild, OnInit } from "@angular/core";
import { CsvService } from "../../services/csv.service";
import { ApiService } from "../../services/api.service";
import { HttpErrorResponse } from "@angular/common/http";
import { of } from "rxjs";
import { catchError } from "rxjs/operators";
import { ErrorResponse } from "../../models/error-response.model";

@Component({
  selector: "app-file-upload",
  templateUrl: "./file-upload.component.html",
  styleUrls: ["./file-upload.component.scss"],
})
export class FileUploadComponent implements OnInit {
  @ViewChild("fileInput") fileInput!: ElementRef;

  fileName = "";
  isLoading = false;
  errorMessage = "";
  successMessage = "";

  backendAvailable = true;

  longestPair: any = null;

  constructor(private csvService: CsvService, private apiService: ApiService) {}

  ngOnInit(): void {
    console.log("Using back-end processing by default");
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length) {
      const file = input.files[0];
      this.fileName = file.name;
      this.processFile(file);
    }
  }

  triggerFileInput() {
    this.fileInput.nativeElement.click();
  }

  processFile(file: File) {
    if (!file.name.toLowerCase().endsWith(".csv")) {
      this.errorMessage = "Please select a CSV file";
      this.successMessage = "";
      this.longestPair = null;
      return;
    }

    this.isLoading = true;
    this.errorMessage = "";
    this.successMessage = "";
    this.longestPair = null;

    this.apiService
      .uploadCsvFile(file)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          console.error("API error:", error);

          try {
            const errorResponse = error.error as ErrorResponse;

            if (errorResponse && errorResponse.message) {
              this.errorMessage = errorResponse.message;
            } else if (typeof error.error === "string") {
              this.errorMessage = error.error;
            } else {
              this.errorMessage = `Error ${error.status}: ${
                error.statusText || "Unknown error"
              }`;
            }
          } catch (e) {
            this.errorMessage = `Error ${error.status}: ${
              error.statusText || "Unknown error"
            }`;
          }

          this.isLoading = false;
          return of(null);
        })
      )
      .subscribe((result) => {
        if (result) {
          this.getLongestPair();
        } else {
          this.isLoading = false;
        }
      });
  }

  getLongestPair() {
    this.apiService
      .getLongestWorkingPair()
      .pipe(
        catchError((error: HttpErrorResponse) => {
          console.error("Error getting longest pair:", error);

          try {
            const errorResponse = error.error as ErrorResponse;

            if (errorResponse && errorResponse.message) {
              this.errorMessage = errorResponse.message;
            } else if (typeof error.error === "string") {
              this.errorMessage = error.error;
            } else {
              this.errorMessage = `Error ${error.status}: ${
                error.statusText || "Unknown error"
              }`;
            }
          } catch (e) {
            this.errorMessage = `Error ${error.status}: ${
              error.statusText || "Unknown error"
            }`;
          }

          this.isLoading = false;
          return of(null);
        })
      )
      .subscribe((result) => {
        if (result) {
          this.longestPair = this.mapBackendResponse(result);
          this.successMessage =
            "File processed successfully! Longest working pair found.";
        }
        this.isLoading = false;
      });
  }

  private mapBackendResponse(backendResponse: any): any {
    const pairIds = backendResponse.pair
      .split(",")
      .map((id: string) => parseInt(id.trim()));

    return {
      employee1Id: pairIds[0],
      employee2Id: pairIds[1],
      totalDaysWorked: backendResponse.totalDays,
      projects: backendResponse.projects.map((project: any) => ({
        employee1Id: project.empId1,
        employee2Id: project.empId2,
        projectId: project.projectId,
        daysWorked: project.daysWorked,
      })),
    };
  }
}
