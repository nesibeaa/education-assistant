import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { ChatService } from './services/chat.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent {
  title = 'aihelper-ui';

  constructor(private router: Router, private chat: ChatService) {}

  get isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  isAuthPage(): boolean {
    const currentUrl = this.router.url;
    return currentUrl === '/login' || currentUrl === '/register';
  }

  newConversation() {
    this.chat.createConversation('Yeni sohbet').subscribe({
      next: (c) => this.router.navigate(['/chat', c.id]),
    });
  }

  logout() {
    localStorage.removeItem('token');
    this.router.navigateByUrl('/');
  }
}