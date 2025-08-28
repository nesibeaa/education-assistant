import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap, Observable } from 'rxjs';

export type AuthResponse = { token: string };
export type MeResponse   = { email: string | null };

@Injectable({ providedIn: 'root' })
export class AuthService {
  private base = '/api/auth';

  constructor(private http: HttpClient) {}

  register(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/register`, { email, password });
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/login`, { email, password }).pipe(
      tap(res => {
        
        localStorage.setItem('token', res.token);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  // Backend /api/me endpointi
  me(): Observable<MeResponse> {
    return this.http.get<MeResponse>('/api/me');
  }
}