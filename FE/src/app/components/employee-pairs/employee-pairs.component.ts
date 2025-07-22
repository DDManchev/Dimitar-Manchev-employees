import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { CsvService } from '../../services/csv.service';
import { LongestPair } from '../../models/employee.model';

@Component({
  selector: 'app-employee-pairs',
  templateUrl: './employee-pairs.component.html',
  styleUrls: ['./employee-pairs.component.scss']
})
export class EmployeePairsComponent implements OnInit, OnDestroy {
  employeePair: LongestPair | null = null;
  private subscription: Subscription | null = null;

  constructor(private csvService: CsvService) {}

  ngOnInit(): void {
    this.subscription = this.csvService.employeePairs$.subscribe(pair => {
      this.employeePair = pair;
    });
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}
