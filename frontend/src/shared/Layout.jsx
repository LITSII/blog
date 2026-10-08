import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';

const NAV = [
  { href: '/', label: '글' },
  { href: '/about.html', label: '소개' },
];

export function Layout({ children, admin = false }) {
  const path = window.location.pathname;
  return (
    <div className="page">
      <header className="site-header">
        <div className="container header-inner">
          <a className="brand" href={admin ? '/admin/' : '/'}>
            {admin ? 'Blog Admin' : 'Blog'}
          </a>
          <nav className="nav">
            {admin ? (
              <a href="/">사이트 보기</a>
            ) : (
              NAV.map((n) => (
                <a key={n.href} href={n.href} className={path === n.href || (n.href === '/' && path === '/index.html') ? 'active' : ''}>
                  {n.label}
                </a>
              ))
            )}
          </nav>
        </div>
      </header>
      <main className={`container main${admin ? ' wide' : ''}`}>{children}</main>
      <footer className="site-footer">
        <div className="container">© {new Date().getFullYear()} Blog</div>
      </footer>
    </div>
  );
}

/** 각 페이지 엔트리에서 호출: MPA라 페이지마다 루트를 따로 마운트 */
export function mount(element) {
  createRoot(document.getElementById('root')).render(<StrictMode>{element}</StrictMode>);
}
