import { useEffect, useId, useState, type FormEvent } from 'react';
import { Link, Navigate, NavLink, Outlet, Route, Routes, useLocation, useNavigate, useParams } from 'react-router-dom';
import {
  ArrowDownRight,
  ArrowLeft,
  ArrowRight,
  ArrowUpRight,
  Bell,
  BookOpen,
  Boxes,
  Check,
  ChevronDown,
  CircleHelp,
  Clock3,
  Command,
  FilePlus2,
  LayoutDashboard,
  LogOut,
  Plus,
  MessageSquareText,
  ShieldCheck,
  Share2,
  Sparkles,
  Workflow,
} from 'lucide-react';
import { api, type AiGenerationResult, type AuthSession, type SystemDesign } from './api';
import { ChatPage, KnowledgePage, NotificationsPage, ReviewsPage } from './FeaturePages';

const starterDiagram = `flowchart LR
  Client[Client Apps] --> Gateway[API Gateway]
  Gateway --> Auth[Auth Service]
  Gateway --> Design[Design Service]
  Design --> DB[(PostgreSQL)]`;

function readSession(): AuthSession | null {
  const token = localStorage.getItem('archai-token');
  const profile = localStorage.getItem('archai-profile');
  if (!token || !profile) return null;
  try {
    return { ...JSON.parse(profile) as Omit<AuthSession, 'token'>, token };
  } catch {
    return null;
  }
}

export default function App() {
  const [session, setSession] = useState<AuthSession | null>(readSession);
  const saveSession = (next: AuthSession) => {
    localStorage.setItem('archai-token', next.token);
    localStorage.setItem('archai-profile', JSON.stringify({
      userId: next.userId,
      email: next.email,
      role: next.role,
    }));
    setSession(next);
  };
  const clearSession = () => {
    localStorage.removeItem('archai-token');
    localStorage.removeItem('archai-profile');
    setSession(null);
  };

  return (
    <Routes>
      <Route path="/login" element={session ? <Navigate to="/" replace /> : <AuthPage mode="login" onAuthenticated={saveSession} />} />
      <Route path="/register" element={session ? <Navigate to="/" replace /> : <AuthPage mode="register" onAuthenticated={saveSession} />} />
      <Route element={session ? <Workspace session={session} onSignOut={clearSession} /> : <Navigate to="/login" replace />}>
        <Route index element={<DashboardPage />} />
        <Route path="designs" element={<DesignsPage />} />
        <Route path="designs/new" element={<DesignEditor />} />
        <Route path="designs/:id" element={<DesignEditor />} />
        <Route path="knowledge" element={<KnowledgePage />} />
        <Route path="reviews" element={<ReviewsPage />} />
        <Route path="chat" element={<ChatPage />} />
        <Route path="notifications" element={<NotificationsPage />} />
      </Route>
      <Route path="*" element={<Navigate to={session ? '/' : '/login'} replace />} />
    </Routes>
  );
}

function AuthPage({ mode, onAuthenticated }: { mode: 'login' | 'register'; onAuthenticated: (session: AuthSession) => void }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const navigate = useNavigate();
  const isRegister = mode === 'register';

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setBusy(true);
    try {
      const session = await api<AuthSession>(`/api/auth/${mode}`, {
        method: 'POST',
        body: JSON.stringify({ email, password, ...(isRegister ? { fullName } : {}) }),
      });
      onAuthenticated(session);
      navigate('/', { replace: true });
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not authenticate. Try again.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="auth-screen">
      <section className="auth-aside">
        <Link className="brand brand-light" to="/login"><span className="brand-mark"><Workflow size={19} /></span>archai</Link>
        <div className="auth-aside-copy">
          <span className="eyebrow">SYSTEMS, MADE LEGIBLE</span>
          <h1>Think in systems.<br /><em>Build with clarity.</em></h1>
          <p>A focused workspace for turning complex requirements into architecture you can reason about.</p>
        </div>
        <div className="auth-aside-foot"><span className="status-dot" /> DESIGN WORKSPACE <span>01 / 04</span></div>
        <div className="auth-grid" aria-hidden="true" />
      </section>
      <section className="auth-panel">
        <div className="auth-form-wrap">
          <span className="eyebrow">{isRegister ? 'CREATE YOUR WORKSPACE' : 'WELCOME BACK'}</span>
          <h2>{isRegister ? 'Start designing.' : 'Sign in to ArchAI.'}</h2>
          <p className="muted">{isRegister ? 'Your architecture work, in one place.' : 'Pick up where your last system left off.'}</p>
          <form className="form-stack" onSubmit={submit}>
            {isRegister && <label>Full name<input autoComplete="name" required value={fullName} onChange={event => setFullName(event.target.value)} placeholder="Ada Lovelace" /></label>}
            <label>Email address<input autoComplete="email" type="email" required value={email} onChange={event => setEmail(event.target.value)} placeholder="you@company.com" /></label>
            <label>Password<input autoComplete={isRegister ? 'new-password' : 'current-password'} type="password" minLength={8} required value={password} onChange={event => setPassword(event.target.value)} placeholder="At least 8 characters" /></label>
            {error && <p className="form-error" role="alert">{error}</p>}
            <button className="button button-primary button-wide" disabled={busy} type="submit">
              {busy ? 'Connecting…' : isRegister ? 'Create account' : 'Sign in'} <ArrowRight size={16} />
            </button>
          </form>
          <p className="auth-switch">{isRegister ? 'Already have an account?' : 'New to ArchAI?'}{' '}
            <Link to={isRegister ? '/login' : '/register'}>{isRegister ? 'Sign in' : 'Create an account'}</Link>
          </p>
          <div className="auth-legal">By continuing, you agree to keep your system designs secure and private.</div>
        </div>
        <div className="auth-panel-foot"><span>ARCHAI / DESIGN SYSTEMS</span><CircleHelp size={15} /> SUPPORT</div>
      </section>
    </main>
  );
}

function Workspace({ session, onSignOut }: { session: AuthSession; onSignOut: () => void }) {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const sectionTitle = pathname.startsWith('/designs') ? 'System designs'
    : pathname === '/knowledge' ? 'Knowledge library'
      : pathname === '/reviews' ? 'Architecture reviews'
        : pathname === '/chat' ? 'Design chat'
          : pathname === '/notifications' ? 'Notifications' : 'Overview';
  function signOut() {
    onSignOut();
    navigate('/login', { replace: true });
  }

  return (
    <div className="workspace">
      <aside className="sidebar">
        <Link className="brand" to="/"><span className="brand-mark"><Workflow size={19} /></span>archai<span className="brand-beta">WORKSPACE</span></Link>
        <div className="workspace-switch"><span className="workspace-glyph">A</span><span><strong>Personal workspace</strong><small>Free plan</small></span><ChevronDown size={15} /></div>
        <span className="nav-label">WORKSPACE</span>
        <nav className="primary-nav" aria-label="Main navigation">
          <NavLink end to="/"><LayoutDashboard size={17} /> Overview</NavLink>
          <NavLink to="/designs"><Boxes size={17} /> System designs</NavLink>
          <NavLink to="/knowledge"><BookOpen size={17} /> Knowledge</NavLink>
          <NavLink to="/reviews"><ShieldCheck size={17} /> Reviews</NavLink>
          <NavLink to="/chat"><MessageSquareText size={17} /> Design chat</NavLink>
          <NavLink to="/notifications"><Bell size={17} /> Notifications</NavLink>
        </nav>
        <div className="sidebar-bottom">
          <div className="sidebar-note"><Sparkles size={16} /><span><strong>Design with intent</strong><small>Keep the architecture simple, until it isn't.</small></span></div>
          <button className="profile-button" onClick={signOut} title="Sign out">
            <span className="avatar">{session.email.slice(0, 1).toUpperCase()}</span>
            <span className="profile-copy"><strong>{session.email}</strong><small>Personal account</small></span>
            <LogOut size={16} />
          </button>
        </div>
      </aside>
      <main className="main-area">
        <header className="topbar">
          <div className="breadcrumb"><span>Workspace</span><span>/</span><strong>{sectionTitle}</strong></div>
          <div className="topbar-actions"><span className="connection-state">API / 8080</span><Link className="icon-button" to="/notifications" title="Notifications" aria-label="Notifications"><Bell size={17} /></Link></div>
        </header>
        <div className="page-content"><Outlet /></div>
        <footer className="app-footer"><span>ARCHAI <span className="footer-dot">·</span> SYSTEM DESIGN</span><span>ENGINEERING WORKSPACE <span className="footer-dot">·</span> {new Date().getFullYear()}</span></footer>
      </main>
    </div>
  );
}

function useDesigns() {
  const [designs, setDesigns] = useState<SystemDesign[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    api<SystemDesign[]>('/api/designs')
      .then(items => { if (active) setDesigns(items); })
      .catch(cause => { if (active) setError(cause instanceof Error ? cause.message : 'Could not load designs.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);
  return { designs, loading, error };
}

function DashboardPage() {
  const { designs, loading, error } = useDesigns();
  const latest = designs.slice(0, 3);
  return (
    <>
      <section className="page-heading dashboard-heading">
        <div><span className="eyebrow">{new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: '2-digit' }).toUpperCase()} <span className="heading-divider">/</span> YOUR WORKSPACE</span><h1>Good systems start<br />with <em>good questions.</em></h1><p>Shape the architecture before the complexity shapes it for you.</p></div>
        <Link className="button button-dark" to="/designs/new"><Plus size={17} /> New system design</Link>
      </section>
      <section className="metric-row" aria-label="Workspace summary">
        <div className="metric"><span className="metric-label">TOTAL DESIGNS</span><strong>{loading ? '—' : String(designs.length).padStart(2, '0')}</strong><span><Boxes size={14} /> IN YOUR WORKSPACE</span></div>
        <div className="metric"><span className="metric-label">RECENTLY UPDATED</span><strong>{loading ? '—' : latest.length.toString().padStart(2, '0')}</strong><span><Clock3 size={14} /> LAST 30 DAYS</span></div>
        <div className="metric metric-accent"><span className="metric-label">NEXT STEP</span><strong>01</strong><span><ArrowDownRight size={14} /> MAP YOUR SYSTEM</span></div>
      </section>
      <section className="section-block">
        <div className="section-heading"><div><span className="eyebrow">YOUR CANVAS</span><h2>Recent designs</h2></div><Link className="text-link" to="/designs">View all <ArrowRight size={15} /></Link></div>
        {error ? <div className="notice notice-error">{error}</div> : loading ? <div className="loading-line">Loading designs…</div> : latest.length ? <DesignGrid designs={latest} /> : <EmptyDesigns />}
      </section>
      <section className="prompt-strip"><div className="prompt-icon"><Command size={18} /></div><div><strong>Start with the constraints</strong><span>Latency, scale, data shape. A clear brief makes a useful design.</span></div><Link to="/designs/new" aria-label="Start a new design"><ArrowRight size={18} /></Link></section>
    </>
  );
}

function DesignsPage() {
  const { designs, loading, error } = useDesigns();
  return (
    <>
      <section className="page-heading compact-heading"><div><span className="eyebrow">WORKSPACE / CANVAS</span><h1>System designs</h1><p>Architecture decisions, captured and ready to evolve.</p></div><Link className="button button-dark" to="/designs/new"><Plus size={17} /> New design</Link></section>
      {error ? <div className="notice notice-error">{error}</div> : loading ? <div className="loading-line">Loading designs…</div> : designs.length ? <DesignGrid designs={designs} /> : <EmptyDesigns />}
    </>
  );
}

function DesignGrid({ designs }: { designs: SystemDesign[] }) {
  return <div className="design-grid">{designs.map((design, index) => (
    <Link className="design-card" to={`/designs/${design.id}`} key={design.id}>
      <div className={`design-card-visual visual-${index % 3}`}><div className="mini-node mini-node-a" /><div className="mini-wire mini-wire-a" /><div className="mini-node mini-node-b" /><div className="mini-wire mini-wire-b" /><div className="mini-node mini-node-c" /><span className="visual-index">0{index + 1}</span></div>
      <div className="design-card-body"><div><span className="card-kind">SYSTEM ARCHITECTURE</span><h3>{design.title}</h3></div><ArrowUpRight size={17} /></div>
      <p>{design.description || 'No brief added yet.'}</p>
      <div className="design-card-foot"><span><Clock3 size={13} /> UPDATED {new Date(design.updatedAt).toLocaleDateString()}</span><span className="card-status"><span /> DRAFT</span></div>
    </Link>
  ))}</div>;
}

function EmptyDesigns() {
  return <div className="empty-state"><div className="empty-glyph"><Workflow size={22} /></div><span className="eyebrow">A CLEAN CANVAS</span><h3>No designs yet.</h3><p>Capture your first system brief and map the pieces that matter.</p><Link className="button button-dark" to="/designs/new"><FilePlus2 size={16} /> Create a design</Link></div>;
}

function DesignEditor() {
  const { id } = useParams();
  const isEditing = Boolean(id);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [diagram, setDiagram] = useState(starterDiagram);
  const [loading, setLoading] = useState(isEditing);
  const [saving, setSaving] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [generationSources, setGenerationSources] = useState<string[]>([]);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    if (!id) return;
    let active = true;
    api<SystemDesign>(`/api/designs/${id}`)
      .then(design => {
        if (!active) return;
        setTitle(design.title);
        setDescription(design.description ?? '');
        setDiagram(design.diagram ?? '');
      })
      .catch(cause => { if (active) setError(cause instanceof Error ? cause.message : 'Could not load design.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [id]);

  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      const design = await api<SystemDesign>(id ? `/api/designs/${id}` : '/api/designs', {
        method: id ? 'PUT' : 'POST',
        body: JSON.stringify({ title, description, diagram }),
      });
      navigate(`/designs/${design.id}`);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not save design.');
    } finally {
      setSaving(false);
    }
  }

  async function generateDraft() {
    if (!title.trim() || !description.trim()) {
      setError('Add a title and problem brief before generating a draft.');
      return;
    }
    setGenerating(true);
    setError('');
    try {
      const result = await api<AiGenerationResult>('/api/ai/generate', {
        method: 'POST',
        body: JSON.stringify({ title, requirements: description }),
      });
      setTitle(result.title || title);
      setDescription(`${result.summary}${result.tradeoffs.length ? `\n\nTrade-offs to consider:\n${result.tradeoffs.map(item => `- ${item}`).join('\n')}` : ''}`);
      setDiagram(result.architectureDiagram);
      setGenerationSources(result.sources.map(source => source.title));
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not generate a draft.');
    } finally {
      setGenerating(false);
    }
  }

  if (loading) return <div className="loading-line">Opening design…</div>;
  return (
    <>
      <div className="editor-topline"><Link className="back-link" to="/designs"><ArrowLeft size={16} /> All designs</Link><span className="save-state"><span className="status-dot" /> {saving ? 'SAVING' : 'LOCAL EDIT'}</span></div>
      <form className="editor-layout" onSubmit={save}>
        <section className="editor-main">
          <div className="editor-toolbar"><span className="eyebrow">{isEditing ? 'SYSTEM DESIGN / EDIT' : 'NEW SYSTEM DESIGN'}</span><button className="button button-outline" type="button" onClick={() => void generateDraft()} disabled={generating}><Sparkles size={15} /> {generating ? 'Generating…' : 'Generate with AI'}</button></div>
          {generationSources.length > 0 && <div className="source-note">Grounded with: {generationSources.join(', ')}</div>}
          <input className="title-input" value={title} onChange={event => setTitle(event.target.value)} placeholder="Name this system" required maxLength={160} />
          <label className="editor-label">PROBLEM BRIEF<textarea value={description} onChange={event => setDescription(event.target.value)} placeholder="What are you building? Include scale, latency, availability, and the constraints that shape the system." rows={5} /></label>
          <label className="editor-label diagram-label">ARCHITECTURE DIAGRAM <span>MERMAID</span><textarea className="code-input" spellCheck={false} value={diagram} onChange={event => setDiagram(event.target.value)} rows={9} /></label>
          <div className="editor-actions">{error && <p className="form-error" role="alert">{error}</p>}<button className="button button-dark" type="submit" disabled={saving}>{saving ? 'Saving…' : <><Check size={16} /> Save design</>}</button></div>
        </section>
        <aside className="preview-panel"><div className="preview-heading"><div><span className="eyebrow">LIVE VIEW</span><h2>Architecture map</h2></div><Share2 size={16} /></div><DiagramPreview source={diagram} /><div className="preview-foot"><span><span className="status-dot" /> PREVIEW READY</span><span>ARCHAI / 01</span></div></aside>
      </form>
    </>
  );
}

function DiagramPreview({ source }: { source: string }) {
  const id = useId().replace(/:/g, '');
  const [svg, setSvg] = useState('');
  const [invalid, setInvalid] = useState(false);

  useEffect(() => {
    let active = true;
    setInvalid(false);
    if (!source.trim()) {
      setSvg('');
      return () => { active = false; };
    }
    void import('mermaid').then(({ default: mermaid }) => {
      if (!active) return;
      mermaid.initialize({ startOnLoad: false, securityLevel: 'strict', theme: 'neutral' });
      return mermaid.render(`archai-diagram-${id}`, source)
        .then(result => { if (active) setSvg(result.svg); });
    }).catch(() => { if (active) { setSvg(''); setInvalid(true); } });
    return () => { active = false; };
  }, [id, source]);

  if (svg) return <div className="diagram-render" dangerouslySetInnerHTML={{ __html: svg }} />;
  return <div className="diagram-fallback"><pre>{source || 'Your architecture will appear here.'}</pre>{invalid && <span>Diagram syntax needs attention.</span>}</div>;
}
