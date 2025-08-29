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
  filteredItems: ConversationSummary[] = [];
  loading = false;
  error = '';
  searchQuery = '';

  constructor(private chat: ChatService, private router: Router) {}

  ngOnInit(): void {
    this.loadHistory();
  }

  formatTimeAgo(date: Date | string): string {
    const now = new Date();
    const targetDate = new Date(date);
    const diffInMs = now.getTime() - targetDate.getTime();
    const diffInMinutes = Math.floor(diffInMs / (1000 * 60));
    const diffInHours = Math.floor(diffInMs / (1000 * 60 * 60));
    const diffInDays = Math.floor(diffInMs / (1000 * 60 * 60 * 24));

    if (diffInMinutes < 1) {
      return 'şimdi';
    } else if (diffInMinutes < 60) {
      return `${diffInMinutes} dk önce`;
    } else if (diffInHours < 24) {
      return `${diffInHours} saat önce`;
    } else if (diffInDays === 1) {
      return 'dün';
    } else if (diffInDays < 7) {
      return `${diffInDays} gün önce`;
    } else {
      // 7 günden fazla ise tarih formatında göster
      return targetDate.toLocaleDateString('tr-TR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      });
    }
  }

  loadHistory() {
    this.loading = true;
    this.error = '';
    this.chat.listConversations().subscribe({
      next: (list) => {
        this.items = list;
        this.filteredItems = list;
      },
      error: () => (this.error = 'Geçmiş alınamadı.'),
      complete: () => (this.loading = false),
    });
  }

  onSearch(event: any) {
    const query = event.target.value.toLowerCase();
    this.searchQuery = query;
    
    if (!query.trim()) {
      this.filteredItems = this.items;
    } else {
      this.filteredItems = this.items.filter(item => 
        item.title?.toLowerCase().includes(query) ||
        item.lastSnippet?.toLowerCase().includes(query)
      );
    }
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

  refresh() {
    this.loadHistory();
  }
}