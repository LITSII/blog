import { useEffect, useState } from 'react';
import { api, currentUser } from './api.js';

/** 관리자 페이지 공통: 로그인 안 돼 있으면 로그인 폼, 돼 있으면 children(user) 렌더 */
export function AdminGate({ children }) {
  const [state, setState] = useState({ loading: true, user: null, error: null });

  const refresh = () =>
    currentUser()
      .then((u) => setState({ loading: false, user: u, error: null }))
      .catch((e) => setState({ loading: false, user: null, error: e.message }));

  useEffect(() => {
    refresh();
  }, []);

  if (state.loading) return <p className="muted">확인 중…</p>;
  if (state.error) return <p className="error">{state.error}</p>;
  if (!state.user) return <LoginForm onSuccess={refresh} />;

  const logout = async () => {
    await api.logout().catch(() => {});
    window.location.href = '/admin/';
  };

  return children({ user: state.user, logout });
}

function LoginForm({ onSuccess }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await api.login(username, password);
      onSuccess();
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  };

  return (
    <div className="login-box">
      <h1>관리자 로그인</h1>
      <form className="form" onSubmit={submit}>
        <label>
          아이디
          <input value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" required />
        </label>
        <label>
          비밀번호
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
        </label>
        {error && <p className="error">{error}</p>}
        <button className="btn btn-primary" disabled={busy}>
          {busy ? '로그인 중…' : '로그인'}
        </button>
      </form>
    </div>
  );
}
