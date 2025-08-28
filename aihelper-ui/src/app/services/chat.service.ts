import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, switchMap } from 'rxjs';
import {
  ChatRequest,
  ChatResponse,
  LinkItem,
  ConversationSummary,
} from './chat.types';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private base = `${environment.apiBase}/chat`;

  constructor(private http: HttpClient) {}

  // FIXED: /conversations/:id/ask yerine /chat/ask
  ask(message: string, conversationId?: number): Observable<ChatResponse> {
    if (conversationId) {
      const body: ChatRequest & { conversationId: number } = { message, conversationId };
      return this.http
        .post<ChatResponse>(`${this.base}/ask`, body)
        .pipe(map((res) => this.normalize(res)));
    }

    // yeni sohbet oluşturup id'yi body'ye veriyoruz
    return this.createConversation('Yeni sohbet').pipe(
      switchMap((c) =>
        this.http
          .post<ChatResponse>(`${this.base}/ask`, { message, conversationId: c.id } as ChatRequest & { conversationId: number })
          .pipe(map((res) => this.normalize(res)))
      )
    );
  }

  createConversation(title?: string): Observable<{ id: number; title: string }> {
    const payload = title ? { title } : {};
    return this.http.post<{ id: number; title: string }>(
      `${this.base}/conversations`,
      payload
    );
  }

  listConversations(): Observable<ConversationSummary[]> {
    return this.http.get<ConversationSummary[]>(`${this.base}/conversations`);
  }

  getConversationMessages(conversationId: number): Observable<ChatResponse[]> {
    return this.http
      .get<ChatResponse[]>(`${this.base}/conversations/${conversationId}/messages`)
      .pipe(map((rows) => rows.map((r) => this.normalize(r))));
  }

  history(limit = 50): Observable<ChatResponse[]> {
    return this.http
      .get<ChatResponse[]>(`${this.base}/history?limit=${limit}`)
      .pipe(map((list) => list.map((r) => this.normalize(r))));
  }

  // helpers
  private normalize(res: ChatResponse): ChatResponse {
    const trimOrNull = (v?: string | null): string | null =>
      v && String(v).trim().length ? String(v).trim() : null;

    const cleanLinks = (arr?: LinkItem[] | null): LinkItem[] | undefined => {
      if (!arr) return undefined;
      const out = arr.filter(
        (x) =>
          !!x &&
          !!x.title &&
          !!String(x.title).trim() &&
          !!x.url &&
          !!String(x.url).trim()
      );
      return out.length ? out : undefined;
    };

    return {
      ...res,
      reply: res.reply ?? '',
      topic: trimOrNull(res.topic),
      status: trimOrNull(res.status),
      difficulty: trimOrNull(res.difficulty),
      nextStep: trimOrNull(res.nextStep),
      resources: cleanLinks(res.resources),
    };
  }
}