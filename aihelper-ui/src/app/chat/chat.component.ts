import { Component, OnInit } from '@angular/core';
import { CommonModule, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ChatService } from '../services/chat.service';
import { ChatResponse } from '../services/chat.types';

type ViewItem =
  | { role: 'user'; text: string; at: Date }
  | { role: 'assistant'; data: ChatResponse; at: Date }
  | { role: 'error'; message?: string; at: Date };

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, NgClass],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css'],
})
export class ChatComponent implements OnInit {
  input = '';
  loading = false;
  history: ViewItem[] = [];
  conversationId!: number;

  constructor(
    private chat: ChatService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.router.navigateByUrl('/history');
      return;
    }
    this.conversationId = id;

    // Bu sohbetin geçmiş mesajlarını getir
    this.chat.getConversationMessages(id).subscribe({
      next: (rows) => {
        
        for (const m of rows) {
          if (m.request) {
            this.history.push({
              role: 'user',
              text: m.request,
              at: new Date(m.createdAt),
            });
          }
          this.history.push({
            role: 'assistant',
            data: m,
            at: new Date(m.createdAt),
          });
        }
      },
    });
  }

  keydown(ev: KeyboardEvent) {
    if (ev.key === 'Enter' && !ev.shiftKey) {
      ev.preventDefault();
      this.send();
    }
  }

  send() {
    const message = this.input.trim();
    if (!message || this.loading) return;

    this.history.push({ role: 'user', text: message, at: new Date() });
    this.input = '';
    this.loading = true;

    this.chat.ask(message, this.conversationId).subscribe({
      next: (res) => {
        this.history.push({
          role: 'assistant',
          data: res,
          at: new Date(res.createdAt ?? Date.now()),
        });
        this.loading = false;
      },
      error: (err) => {
        console.error('Chat error:', err);
        this.history.push({
          role: 'error',
          message: 'Üzgünüm, bir hata oluştu.',
          at: new Date(),
        });
        this.loading = false;
      },
    });
  }

  has(v?: string | null): boolean {
    return !!(v && String(v).trim().length);
  }

  toPercent(val: number | null | undefined): string {
    if (val == null || Number.isNaN(val)) return '—';
    const pct = Math.round(val * 100);
    return `${pct}%`;
  }
}