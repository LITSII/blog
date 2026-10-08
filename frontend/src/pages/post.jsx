import { useEffect, useMemo, useState } from 'react';
import { Layout, mount } from '../shared/Layout.jsx';
import { api } from '../shared/api.js';
import { formatDate, getParam } from '../shared/format.js';
import { renderMarkdown } from '../shared/markdown.js';

function PostPage() {
  const id = getParam('id');
  const [post, setPost] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!id || !/^\d+$/.test(id)) {
      setError('잘못된 주소입니다.');
      return;
    }
    api
      .getPost(id)
      .then((p) => {
        setPost(p);
        document.title = `${p.title} - 블로그`;
      })
      .catch((e) => setError(e.status === 404 ? '글을 찾을 수 없습니다.' : e.message));
  }, [id]);

  const html = useMemo(() => (post ? renderMarkdown(post.content) : ''), [post]);

  return (
    <Layout>
      {error && (
        <>
          <p className="error">{error}</p>
          <a href="/">← 목록으로</a>
        </>
      )}
      {!post && !error && <p className="muted">불러오는 중…</p>}
      {post && (
        <article>
          <h1>{post.title}</h1>
          <div className="meta">{formatDate(post.publishedAt)}</div>
          {/* renderMarkdown은 DOMPurify로 정화된 HTML만 반환 */}
          <div className="prose" dangerouslySetInnerHTML={{ __html: html }} />
          <p style={{ marginTop: 48 }}>
            <a href="/">← 목록으로</a>
          </p>
        </article>
      )}
    </Layout>
  );
}

mount(<PostPage />);
