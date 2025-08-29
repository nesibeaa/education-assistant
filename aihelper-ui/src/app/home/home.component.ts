import { Component, OnInit, AfterViewInit } from '@angular/core';
import { RouterLink } from '@angular/router';

declare var Chart: any;

@Component({
  standalone: true,
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit, AfterViewInit {
  
  ngOnInit() {
    // Component initialization
  }

  ngAfterViewInit() {
    this.initChart();
  }

  private initChart() {
    const ctx = document.getElementById('progressChart') as HTMLCanvasElement;
    if (!ctx) return;

    const style = getComputedStyle(document.body);
    const border = style.color;
    const grid = 'rgba(120,120,120,0.15)';

    new Chart(ctx, {
      type: 'line',
      data: {
        labels: ['Pzt','Sal','Çar','Per','Cum','Cmt','Paz'],
        datasets: [{
          label: 'Dakika',
          data: [30, 42, 25, 50, 38, 60, 55],
          tension: 0.4,
          borderColor: '#2563eb',
          backgroundColor: 'rgba(37,99,235,0.2)',
          fill: true,
          pointRadius: 3,
          pointBackgroundColor: '#2563eb'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: { enabled: true, intersect: false }
        },
        scales: {
          x: {
            grid: { display: false },
            ticks: { color: document.body.className?.includes('dark') ? '#a3a3a3' : '#6b7280' }
          },
          y: {
            grid: { color: 'rgba(120,120,120,0.15)' },
            ticks: { color: document.body.className?.includes('dark') ? '#a3a3a3' : '#6b7280' }
          }
        }
      }
    });
  }
}