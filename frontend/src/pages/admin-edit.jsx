import { useEffect, useMemo, useState } from 'react';
import { Layout, mount } from '../shared/Layout.jsx';
import { AdminGate } from '../shared/AdminGate.jsx';
import { api } from '../shared/api.js';
import { getParam } from '../shared/format.js';
import { renderMarkdown } from '../shared/markdown.js';

const EMPTY = { title: '', summary: '', content: '', status: 'DRAFT' };

function Editor() {
  const idParam = getParam('id');
  const id = idParam && /^\d+$/.test(idParam) ? idParam : null;
  const [form, setForm] = useState(id ? null : EMPTY);
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!id) return;
    api
      .adminGet(id)
      .then((p) => setForm({ title: p.title, summary: p.summary ?? '', content: p.content, status: p.status }))
      .catch((e) => setError(e.message));
  }, [id]);

  const preview = useMemo(() => (form ? renderMarkdown(form.content) : ''), [form?.content]);

  if (error && !form) return <p className="error">{error}</p>;
  if (!form) return <p className="muted">불러오는 중…</p>;

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  const save = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      if (id) {
        await api.adminUpdate(id, form);
      } else {
        const created = await api.adminCreate(form);
        window.history.replaceState(null, '', `/admin/edit.html?id=${created.id}`);
      }
      window.location.href = '/admin/';
    } catch (err) {
      setError(err.message);
      setSaving(false);
    }
  };

  return (
    <form className="form" onSubmit={save}>
      <div className="row">
        <h1>{id ? '글 수정' : '새 글'}</h1>
        <span className="spacer" />
        <a className="btn" href="/admin/">
          취소
        </a>
        <button className="btn btn-primary" disabled={saving}>
          {saving ? '저장 중…' : '저장'}
        </button>
      </div>
      {error && <p className="error">{error}</p>}
      <label>
        제목
        <input value={form.title} onChange={set('title')} maxLength={200} required />
      </label>
      <label>
        요약 (목록에 표시)
        <input value={form.summary} onChange={set('summary')} maxLength={500} />
      </label>
      <label>
        상태
        <select value={form.status} onChange={set('status')}>
          <option value="DRAFT">초안</option>
          <option value="PUBLISHED">발행</option>
        </select>
      </label>
      <div className="editor-grid">
        <label>
          본문 (Markdown)
          <textarea value={form.content} onChange={set('content')} required />
        </label>
        <div>
          <div className="meta" style={{ marginBottom: 6 }}>
            미리보기
          </div>
          <div className="preview">
            <div className="prose" dangerouslySetInnerHTML={{ __html: preview }} />
          </div>
        </div>
      </div>
    </form>
  );
}

function AdminEdit() {
  return (
    <Layout admin>
      <AdminGate>{() => <Editor />}</AdminGate>
    </Layout>
  );
}

mount(<AdminEdit />);
