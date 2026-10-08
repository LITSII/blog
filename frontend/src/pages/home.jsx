import { useEffect, useState } from 'react';
import { Layout, mount } from '../shared/Layout.jsx';
import { api } from '../shared/api.js';
import { formatDate, getParam } from '../shared/format.js';

const PAGE_SIZE = 10;

function Home() {
  const page = Math.max(parseInt(getParam('page') ?? '0', 10) || 0, 0);
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.listPosts(page, PAGE_SIZE).then(setData).catch((e) => setError(e.message));
  }, [page]);

  return (
    <Layout>
      <h1>최근 글</h1>
      {error && <p className="error">{error}</p>}
      {!data && !error && <p className="muted">불러오는 중…</p>}
      {data && data.items.length === 0 && <p className="muted">아직 작성된 글이 없습니다.</p>}
      {data && data.items.length > 0 && (
        <>
          <ul className="post-list">
            {data.items.map((p) => (
              <li key={p.id} className="post-item">
                <h2>
                  <a href={`/post.html?id=${p.id}`}>{p.title}</a>
                </h2>
                {p.summary && <p>{p.summary}</p>}
                <div className="meta">{formatDate(p.publishedAt)}</div>
              </li>
            ))}
          </ul>
          <div className="pager">
            {page > 0 ? <a href={`/?page=${page - 1}`}>← 최신 글</a> : <span />}
            {page + 1 < data.totalPages ? <a href={`/?page=${page + 1}`}>이전 글 →</a> : <span />}
          </div>
        </>
      )}
    </Layout>
  );
}

mount(<Home />);
