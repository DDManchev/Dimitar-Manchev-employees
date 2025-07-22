import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { EmployeePair, LongestPair } from '../models/employee.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  uploadCsvFile(file: File): Observable<LongestPair> {
    const formData = new FormData();
    formData.append('file', file);
    
    return this.http.post<LongestPair>(`${this.apiUrl}/api/employees/upload`, formData);
  }

  getLongestWorkingPair(): Observable<LongestPair> {
    return this.http.get<LongestPair>(`${this.apiUrl}/api/employees/longest-pair`);
  }
}
