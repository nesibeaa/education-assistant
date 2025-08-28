import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { ChatService } from '../services/chat.service';
import { ConversationSummary } from '../services/chat.types';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './history.component.html',
  styleUrls: ['./history.component.scss'],
})
export class HistoryComponent implements OnInit {
  items: ConversationSummary[] = [];
  loading = false;
  error = '';

  constructor(private chat: ChatService, private router: Router) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh() {
    this.loading = true;
    this.error = '';
    this.chat.listConversations().subscribe({
      next: (list) => (this.items = list),
      error: () => (this.error = 'Geçmiş alınamadı.'),
      complete: () => (this.loading = false),
    });
  }

  open(item: ConversationSummary) {
    this.router.navigate(['/chat', item.id]);
  }

  newChat() {
    this.chat.createConversation().subscribe({
      next: (r) => this.router.navigate(['/chat', r.id]),
      error: () => (this.error = 'Yeni sohbet oluşturulamadı.'),
    });
  }
}