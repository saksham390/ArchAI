export interface AuthSession {
  token: string;
  userId: string;
  email: string;
  role: string;
}

export interface SystemDesign {
  id: number;
  title: string;
  description: string;
  diagram: string;
  createdAt: string;
  updatedAt: string;
}

export interface KnowledgeDocument {
  id: number;
  title: string;
  content: string;
  sourceUrl?: string;
  createdAt: string;
}

export interface AiGenerationResult {
  title: string;
  summary: string;
  architectureDiagram: string;
  tradeoffs: string[];
  sources: { id: number; title: string; sourceUrl?: string }[];
}

export interface ArchitectureReview {
  id: number;
  designId: number;
  title: string;
  score: number;
  summary: string;
  findings: { severity: string; category: string; title: string; detail: string; recommendation: string }[];
  createdAt: string;
}

export interface ChatConversation {
  id: number;
  title: string;
  createdAt: string;
  updatedAt: string;
}

export interface ChatMessage {
  id: number;
  role: 'user' | 'assistant';
  content: string;
  createdAt: string;
}

export interface AppNotification {
  id: number;
  type: string;
  title: string;
  message: string;
  sourceId?: number;
  read: boolean;
  createdAt: string;
}

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  const token = localStorage.getItem('archai-token');
  if (token) headers.set('Authorization', `Bearer ${token}`);
  if (options.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');

  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  if (!response.ok) {
    const body = await response.json().catch(() => null) as { message?: string } | null;
    throw new Error(body?.message ?? `Request failed (${response.status})`);
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}
