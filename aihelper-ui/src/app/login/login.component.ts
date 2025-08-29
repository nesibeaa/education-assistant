import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  standalone: true,
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  email = '';
  password = '';
  loginText = '';
  meText = '';
  emailError = '';
  passwordError = '';
  activeTab = 'login';
  rememberMe = false;
  acceptTerms = false;
  showPassword = false;

  constructor(private auth: AuthService, private router: Router) {}

  setActiveTab(tab: string) {
    this.activeTab = tab;
    this.clearErrors();
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
    const passwordInput = document.getElementById('password') as HTMLInputElement;
    if (passwordInput) {
      passwordInput.type = this.showPassword ? 'text' : 'password';
    }
  }

  toggleRememberMe() {
    this.rememberMe = !this.rememberMe;
  }

  toggleAcceptTerms() {
    this.acceptTerms = !this.acceptTerms;
  }

  clearErrors() {
    this.emailError = '';
    this.passwordError = '';
    this.loginText = '';
    this.meText = '';
  }

  validateForm(): boolean {
    this.clearErrors();
    let isValid = true;

    if (this.activeTab === 'login') {
      if (!this.email) {
        this.emailError = 'E-posta gerekli';
        isValid = false;
      } else if (!this.email.includes('@')) {
        this.emailError = 'Geçerli bir e-posta girin';
        isValid = false;
      }

      if (!this.password) {
        this.passwordError = 'Şifre gerekli';
        isValid = false;
      } else if (this.password.length < 6) {
        this.passwordError = 'Şifre en az 6 karakter olmalı';
        isValid = false;
      }
    } else {
      if (!this.email) {
        this.emailError = 'E-posta gerekli';
        isValid = false;
      } else if (!this.email.includes('@')) {
        this.emailError = 'Geçerli bir e-posta girin';
        isValid = false;
      }

      if (!this.password) {
        this.passwordError = 'Şifre gerekli';
        isValid = false;
      } else if (this.password.length < 6) {
        this.passwordError = 'Şifre en az 6 karakter olmalı';
        isValid = false;
      }

      if (!this.acceptTerms) {
        this.emailError = 'Şartları kabul etmelisiniz';
        isValid = false;
      }
    }

    return isValid;
  }

  onLogin() {
    if (!this.validateForm()) return;

    this.loginText = 'Giriş yapılıyor...';

    this.auth.login(this.email, this.password).subscribe({
      next: (response) => {
        this.loginText = 'Giriş başarılı!';
        this.router.navigateByUrl('/dashboard');
      },
      error: (error) => {
        this.loginText = 'Giriş hatası: ' + (error?.error?.error ?? error.message ?? error.statusText);
      }
    });
  }

  onRegister() {
    if (!this.validateForm()) return;

    this.loginText = 'Kayıt olunuyor...';

    this.auth.register(this.email, this.password).subscribe({
      next: (response) => {
        this.loginText = 'Kayıt başarılı! Giriş yapılıyor...';
        // Otomatik giriş yap
        this.auth.login(this.email, this.password).subscribe({
          next: (loginResponse) => {
            this.loginText = 'Hoş geldin!';
            this.router.navigateByUrl('/dashboard');
          },
          error: (loginError) => {
            this.loginText = 'Giriş hatası: ' + (loginError?.error?.error ?? loginError.message ?? loginError.statusText);
          }
        });
      },
      error: (error) => {
        this.loginText = 'Kayıt hatası: ' + (error?.error?.error ?? error.message ?? error.statusText);
      }
    });
  }

  goToDemo() {
    this.router.navigateByUrl('/dashboard');
  }
}