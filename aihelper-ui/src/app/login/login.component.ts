import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { catchError, switchMap } from 'rxjs/operators';
import { of } from 'rxjs';

import { AuthService } from '../services/auth.service';

@Component({
  standalone: true,
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styles: [``]
})
export class LoginComponent {
  email = '';
  password = '';

  loginText = '';
  meText = '';

  constructor(private auth: AuthService, private router: Router) {}

  /** "Register" butonu artık ayrı sayfaya gider */
  onRegister() {
    this.router.navigateByUrl('/register');
  }

  onLogin() {
    this.loginText = 'Logging in...';
    this.meText = '';

    this.auth.login(this.email, this.password).pipe(
      switchMap(() => {
        this.loginText = 'Login OK. Token stored in localStorage.';
        return this.auth.me();
      }),
      catchError(err => {
        this.loginText = 'Login error: ' + (err?.error?.error ?? err.message ?? err.statusText);
        return of(null);
      })
    ).subscribe(me => {
      if (!me) return;
      this.meText = 'Me OK → email: ' + (me?.email ?? '(null)');
      this.router.navigateByUrl('/chat');
    });
  }
}