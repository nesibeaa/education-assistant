import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  standalone: true,
  selector: 'app-register',
  imports: [CommonModule, FormsModule],
  templateUrl: './register.component.html',
  styles: [``]
})
export class RegisterComponent {
  email = '';
  password = '';
  note = '';

  constructor(private auth: AuthService, private router: Router) {}

  onSubmit() {
    this.note = 'Registering...';

    this.auth.register(this.email, this.password).pipe(
      switchMap(() => {
        this.note = 'Registered. Logging in...';
        return this.auth.login(this.email, this.password);
      }),
      switchMap(() => this.auth.me()),
      catchError(err => {
        this.note = 'Register error: ' + (err?.error?.error ?? err.message ?? err.statusText);
        return of(null);
      })
    ).subscribe(me => {
      if (!me) return;
      this.note = 'Welcome ' + (me?.email ?? '');
      this.router.navigateByUrl('/chat');
    });
  }

  goLogin() {
    this.router.navigateByUrl('/login');
  }
}