import { useEffect, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, BookOpen, Check, CircleAlert, FilePlus2, Plus, Send, ShieldCheck, Sparkles } from 'lucide-react';
import {
  api,
  type AppNotification,
  type ArchitectureReview,
  type ChatConversation,
  type ChatMessage,
  type KnowledgeDocument,
  type SystemDesign,
} from './api';

export function KnowledgePage() {
  const [documents, setDocuments] = useState<KnowledgeDocument[]>([]);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [sourceUrl, setSourceUrl] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function loadDocuments() {
    try {
      setDocuments(await api<KnowledgeDocument[]>('/api/knowledge'));
      setError('');
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not load knowledge.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void loadDocuments(); }, []);

  async function addDocument(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      await api<KnowledgeDocument>('/api/knowledge', {
        method: 'POST',
        body: JSON.stringify({ title, content, sourceUrl: sourceUrl || null }),
      });
      setTitle('');
      setContent('');
      setSourceUrl('');
      await loadDocuments();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not save the document.');
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <section className="page-heading compact-heading"><div><span className="eyebrow">WORKSPACE / REFERENCES</span><h1>Knowledge library</h1><p>Keep design notes and references close to the systems they inform.</p></div></section>
      <div className="feature-columns">
        <section className="feature-panel">
          <div className="section-heading"><div><span className="eyebrow">SOURCE MATERIAL</span><h2>Add a reference</h2></div><BookOpen size={18} /></div>
          <form className="feature-form" onSubmit={addDocument}>
            <label>Title<input required maxLength={200} value={title} onChange={event => setTitle(event.target.value)} placeholder="e.g. API reliability standards" /></label>
            <label>Source URL <span className="optional-label">OPTIONAL</span><input type="url" maxLength={2048} value={sourceUrl} onChange={event => setSourceUrl(event.target.value)} placeholder="https://…" /></label>
            <label>Notes or excerpt<textarea required maxLength={50000} rows={8} value={content} onChange={event => setContent(event.target.value)} placeholder="Paste design principles, constraints, or a useful excerpt…" /></label>
            {error && <p className="form-error" role="alert">{error}</p>}
            <button className="button button-dark" type="submit" disabled={saving}>{saving ? 'Saving…' : <><Plus size={15} /> Add to library</>}</button>
          </form>
        </section>
        <section className="feature-panel knowledge-list-panel">
          <div className="section-heading"><div><span className="eyebrow">PRIVATE TO YOUR ACCOUNT</span><h2>Saved references</h2></div><span className="count-chip">{documents.length}</span></div>
          {loading ? <p className="loading-line">Loading references…</p> : documents.length ? <div className="knowledge-list">{documents.map(document => (
            <article className="knowledge-item" key={document.id}>
              <div className="knowledge-item-icon"><BookOpen size={16} /></div>
              <div className="knowledge-item-content"><strong>{document.title}</strong><p>{document.content}</p>{document.sourceUrl && <a href={document.sourceUrl} target="_blank" rel="noreferrer">Open source <ArrowRight size={12} /></a>}</div>
            </article>
          ))}</div> : <div className="feature-empty"><BookOpen size={21} /><strong>No references saved</strong><span>Add concise, relevant notes to ground AI responses.</span></div>}
        </section>
      </div>
    </>
  );
}

export function ReviewsPage() {
  const [designs, setDesigns] = useState<SystemDesign[]>([]);
  const [reviews, setReviews] = useState<ArchitectureReview[]>([]);
  const [loading, setLoading] = useState(true);
  const [runningId, setRunningId] = useState<number | null>(null);
  const [error, setError] = useState('');

  async function loadReviews() {
    try {
      const [savedReviews, savedDesigns] = await Promise.all([
        api<ArchitectureReview[]>('/api/reviews'),
        api<SystemDesign[]>('/api/designs'),
      ]);
      setReviews(savedReviews);
      setDesigns(savedDesigns);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not load reviews.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void loadReviews(); }, []);

  async function reviewDesign(designId: number) {
    setRunningId(designId);
    setError('');
    try {
      const review = await api<ArchitectureReview>('/api/reviews', {
        method: 'POST',
        body: JSON.stringify({ designId }),
      });
      setReviews(current => [review, ...current]);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not review this design.');
    } finally {
      setRunningId(null);
    }
  }

  return (
    <>
      <section className="page-heading compact-heading"><div><span className="eyebrow">WORKSPACE / QUALITY</span><h1>Architecture reviews</h1><p>Surface trade-offs and risks before they become incidents.</p></div></section>
      {error && <div className="notice notice-error feature-notice">{error}</div>}
      <section className="section-block feature-section">
        <div className="section-heading"><div><span className="eyebrow">REVIEW QUEUE</span><h2>Choose a design</h2></div><ShieldCheck size={18} /></div>
        {loading ? <p className="loading-line">Loading designs…</p> : designs.length ? <div className="review-candidates">{designs.map(design => (
          <div className="review-candidate" key={design.id}><div><span className="card-kind">SYSTEM DESIGN</span><strong>{design.title}</strong></div><button className="button button-outline" disabled={runningId !== null || !design.diagram} onClick={() => void reviewDesign(design.id)}>{runningId === design.id ? 'Reviewing…' : <><Sparkles size={14} /> Run review</>}</button></div>
        ))}</div> : <div className="feature-empty"><FilePlus2 size={20} /><strong>No designs available</strong><Link to="/designs/new">Create a design <ArrowRight size={13} /></Link></div>}
      </section>
      <section className="section-block feature-section">
        <div className="section-heading"><div><span className="eyebrow">SAVED ANALYSIS</span><h2>Review history</h2></div></div>
        {reviews.length ? <div className="review-list">{reviews.map(review => (
          <article className="review-item" key={review.id}>
            <div className="review-score"><strong>{review.score}</strong><span>/ 100</span></div>
            <div className="review-copy"><div className="review-title-row"><h3>{review.title}</h3><time>{new Date(review.createdAt).toLocaleDateString()}</time></div><p>{review.summary}</p>
              {review.findings.map((finding, index) => <div className="finding-row" key={`${review.id}-${index}`}><span className={`severity severity-${finding.severity.toLowerCase()}`}>{finding.severity}</span><div><strong>{finding.title}</strong><p>{finding.recommendation}</p></div></div>)}
            </div>
          </article>
        ))}</div> : !loading && <div className="feature-empty"><CircleAlert size={20} /><strong>No reviews yet</strong><span>Select a diagram above to start a review.</span></div>}
      </section>
    </>
  );
}

export function ChatPage() {
  const [conversations, setConversations] = useState<ChatConversation[]>([]);
  const [activeId, setActiveId] = useState<number | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [draft, setDraft] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');

  async function loadConversations() {
    try {
      const items = await api<ChatConversation[]>('/api/chat/conversations');
      setConversations(items);
      if (items.length && activeId === null) setActiveId(items[0].id);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not load conversations.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void loadConversations(); }, []);

  useEffect(() => {
    if (activeId === null) {
      setMessages([]);
      return;
    }
    let active = true;
    api<ChatMessage[]>(`/api/chat/conversations/${activeId}/messages`)
      .then(items => { if (active) setMessages(items); })
      .catch(cause => { if (active) setError(cause instanceof Error ? cause.message : 'Could not load messages.'); });
    return () => { active = false; };
  }, [activeId]);

  async function createConversation() {
    setError('');
    try {
      const item = await api<ChatConversation>('/api/chat/conversations', {
        method: 'POST',
        body: JSON.stringify({ title: `Design discussion ${new Date().toLocaleDateString()}` }),
      });
      setConversations(current => [item, ...current]);
      setActiveId(item.id);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not create a conversation.');
    }
  }

  async function sendMessage(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const message = draft.trim();
    if (!message || sending) return;
    setSending(true);
    setError('');
    try {
      let conversationId = activeId;
      if (conversationId === null) {
        const item = await api<ChatConversation>('/api/chat/conversations', {
          method: 'POST',
          body: JSON.stringify({ title: message.slice(0, 80) }),
        });
        conversationId = item.id;
        setConversations(current => [item, ...current]);
        setActiveId(item.id);
      }
      await api<ChatMessage>(`/api/chat/conversations/${conversationId}/messages`, {
        method: 'POST',
        body: JSON.stringify({ message }),
      });
      setDraft('');
      setMessages(await api<ChatMessage[]>(`/api/chat/conversations/${conversationId}/messages`));
      void loadConversations();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not send the message.');
    } finally {
      setSending(false);
    }
  }

  const activeConversation = conversations.find(item => item.id === activeId);
  return (
    <>
      <section className="page-heading compact-heading"><div><span className="eyebrow">WORKSPACE / THINKING PARTNER</span><h1>Design chat</h1><p>Explore trade-offs with context from your saved references.</p></div><button className="button button-dark" onClick={() => void createConversation()}><Plus size={15} /> New conversation</button></section>
      {error && <div className="notice notice-error feature-notice">{error}</div>}
      <section className="chat-workspace">
        <aside className="chat-conversations"><span className="eyebrow">CONVERSATIONS</span>{loading ? <p className="loading-line">Loading…</p> : conversations.length ? conversations.map(item => <button className={`conversation-link ${item.id === activeId ? 'selected' : ''}`} key={item.id} onClick={() => setActiveId(item.id)}><span>{item.title}</span><small>{new Date(item.updatedAt).toLocaleDateString()}</small></button>) : <p className="conversation-empty">No conversations yet.</p>}</aside>
        <div className="chat-panel">
          <div className="chat-panel-head"><div><span className="eyebrow">ARCHAI ASSISTANT</span><strong>{activeConversation?.title ?? 'Start a conversation'}</strong></div><span className="chat-model"><span className="status-dot" /> GEMINI</span></div>
          <div className="chat-messages">{messages.length ? messages.map(message => <article className={`chat-message ${message.role}`} key={message.id}><span className="message-role">{message.role === 'assistant' ? 'ARCHAI' : 'YOU'}</span><p>{message.content}</p></article>) : <div className="chat-welcome"><Sparkles size={21} /><strong>What are you designing?</strong><span>Ask about scale, data flow, availability, or a decision you are weighing.</span></div>}</div>
          <form className="chat-compose" onSubmit={sendMessage}><textarea value={draft} onChange={event => setDraft(event.target.value)} placeholder="Ask a system design question…" rows={2} maxLength={6000} /><button className="button button-dark" type="submit" disabled={sending || !draft.trim()} aria-label="Send message">{sending ? 'Thinking…' : <Send size={15} />}</button></form>
        </div>
      </section>
    </>
  );
}

export function NotificationsPage() {
  const [notifications, setNotifications] = useState<AppNotification[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api<AppNotification[]>('/api/notifications')
      .then(setNotifications)
      .catch(cause => setError(cause instanceof Error ? cause.message : 'Could not load notifications.'))
      .finally(() => setLoading(false));
  }, []);

  async function markRead(id: number) {
    try {
      const updated = await api<AppNotification>(`/api/notifications/${id}/read`, { method: 'PATCH' });
      setNotifications(current => current.map(item => item.id === id ? updated : item));
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not update notification.');
    }
  }

  return (
    <>
      <section className="page-heading compact-heading"><div><span className="eyebrow">WORKSPACE / ACTIVITY</span><h1>Notifications</h1><p>Updates from your design workspace.</p></div></section>
      {error && <div className="notice notice-error feature-notice">{error}</div>}
      {loading ? <p className="loading-line">Loading notifications…</p> : notifications.length ? <div className="notification-list">{notifications.map(item => (
        <article className={`notification-item ${item.read ? '' : 'unread'}`} key={item.id}><span className="notification-mark" /><div className="notification-copy"><span className="eyebrow">{item.type.replace(/_/g, ' ')} <span className="heading-divider">/</span> {new Date(item.createdAt).toLocaleString()}</span><strong>{item.title}</strong><p>{item.message}</p></div>{!item.read && <button className="button button-outline" onClick={() => void markRead(item.id)}><Check size={14} /> Mark read</button>}</article>
      ))}</div> : <div className="feature-empty notification-empty"><Check size={21} /><strong>You are all caught up</strong><span>New activity will appear here.</span></div>}
    </>
  );
}
