import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  standalone: true,
  selector: 'app-register',
  imports: [CommonModule, FormsModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  email = '';
  password = '';
  note = '';
  acceptTerms = false;

  constructor(private auth: AuthService, private router: Router) {}

  toggleAcceptTerms() {
    this.acceptTerms = !this.acceptTerms;
  }

  clearErrors() {
    this.note = '';
  }

  validateForm(): boolean {
    this.clearErrors();
    let isValid = true;

    if (!this.email) {
      this.note = 'E-posta gerekli';
      isValid = false;
    } else if (!this.email.includes('@')) {
      this.note = 'Geçerli bir e-posta girin';
      isValid = false;
    }

    if (!this.password) {
      this.note = 'Şifre gerekli';
      isValid = false;
    } else if (this.password.length < 6) {
      this.note = 'Şifre en az 6 karakter olmalı';
      isValid = false;
    }

    if (!this.acceptTerms) {
      this.note = 'Şartları kabul etmelisiniz';
      isValid = false;
    }

    return isValid;
  }

  onSubmit() {
    if (!this.validateForm()) return;

    this.note = 'Kayıt olunuyor...';

    this.auth.register(this.email, this.password).subscribe({
      next: (response) => {
        this.note = 'Kayıt başarılı! Giriş yapılıyor...';
        // Otomatik giriş yap
        this.auth.login(this.email, this.password).subscribe({
          next: (loginResponse) => {
            this.note = 'Hoş geldin!';
            this.router.navigateByUrl('/dashboard');
          },
          error: (loginError) => {
            this.note = 'Giriş hatası: ' + (loginError?.error?.error ?? loginError.message ?? loginError.statusText);
          }
        });
      },
      error: (error) => {
        this.note = 'Kayıt hatası: ' + (error?.error?.error ?? error.message ?? error.statusText);
      }
    });
  }
}