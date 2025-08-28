
export type ChatRequest = {
  message: string;
  
  conversationId?: number;
  
  title?: string;
};

export type LinkItem = { title: string; url: string };

export type ChatResponse = {
  id: number;
  userId: number;
  request: string;
  reply: string;
  status?: string | null;
  topic?: string | null;
  difficulty?: string | null;
  nextStep?: string | null;
  confidence?: number | null;
  createdAt: string;   // ISO string
  source: string;      // "Gemini" vb.
  resources?: LinkItem[];
};


export type ConversationSummary = {
  id: number;
  title: string;
  lastSnippet: string;
  messageCount: number;
  updatedAt: string; // ISO
};

// UI geçmişi için
export type HistoryItem =
  | { role: 'user'; text: string }
  | { role: 'assistant'; data: ChatResponse }
  | { role: 'error'; message?: string };