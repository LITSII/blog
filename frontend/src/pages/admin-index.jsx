import { useEffect, useState } from 'react';
import { Layout, mount } from '../shared/Layout.jsx';
import { AdminGate } from '../shared/AdminGate.jsx';
import { api } from '../shared/api.js';
import { formatDate } from '../shared/format.js';

function PostTable() {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);

  const load = () => api.adminList(0, 50).then(setData).catch((e) => setError(e.message));
  useEffect(() => {
    load();
  }, []);

  const remove = async (p) => {
    if (!window.confirm(`"${p.title}" 글을 삭제할까요? 되돌릴 수 없습니다.`)) return;
    try {
      await api.adminDelete(p.id);
      load();
    } catch (e) {
      setError(e.message);
    }
  };

  if (error) return <p className="error">{error}</p>;
  if (!data) return <p className="muted">불러오는 중…</p>;
  if (data.items.length === 0) return <p className="muted">글이 없습니다. 첫 글을 작성해 보세요.</p>;

  return (
    <table className="table">
      <thead>
        <tr>
          <th>제목</th>
          <th>상태</th>
          <th>수정일</th>
          <th />
        </tr>
      </thead>
      <tbody>
        {data.items.map((p) => (
          <tr key={p.id}>
            <td>
              <a href={`/admin/edit.html?id=${p.id}`}>{p.title}</a>
            </td>
            <td>
              <span className={`badge ${p.status === 'PUBLISHED' ? 'published' : ''}`}>
                {p.status === 'PUBLISHED' ? '발행' : '초안'}
              </span>
            </td>
            <td className="meta">{formatDate(p.updatedAt)}</td>
            <td style={{ textAlign: 'right' }}>
              <button className="btn btn-danger" onClick={() => remove(p)}>
                삭제
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function AdminIndex() {
  return (
    <Layout admin>
      <AdminGate>
        {({ user, logout }) => (
          <>
            <div className="row">
              <h1>글 관리</h1>
              <span className="spacer" />
              <span className="meta">{user.username}</span>
              <button className="btn" onClick={logout}>
                로그아웃
              </button>
              <a className="btn btn-primary" href="/admin/edit.html">
                새 글
              </a>
            </div>
            <PostTable />
          </>
        )}
      </AdminGate>
    </Layout>
  );
}

mount(<AdminIndex />);
