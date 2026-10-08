// 백엔드는 같은 오리진의 /api 아래에 있다고 가정 (운영: Nginx 프록시, 개발: Vite 프록시)
const API_BASE = import.meta.env.VITE_API_BASE ?? '';

function readCookie(name) {
  const m = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'));
  return m ? decodeURIComponent(m[1]) : null;
}

export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

async function request(method, path, body) {
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (method !== 'GET') {
    const token = readCookie('XSRF-TOKEN');
    if (token) headers['X-XSRF-TOKEN'] = token;
  }

  const res = await fetch(API_BASE + path, {
    method,
    headers,
    credentials: 'include',
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    throw new ApiError(res.status, data?.detail || data?.message || `요청 실패 (${res.status})`);
  }
  return data;
}

export const api = {
  // 공개
  listPosts: (page = 0, size = 10) => request('GET', `/api/posts?page=${page}&size=${size}`),
  getPost: (id) => request('GET', `/api/posts/${encodeURIComponent(id)}`),

  // 인증
  me: () => request('GET', '/api/auth/me'),
  login: (username, password) => request('POST', '/api/auth/login', { username, password }),
  logout: () => request('POST', '/api/auth/logout'),

  // 관리자
  adminList: (page = 0, size = 20) => request('GET', `/api/admin/posts?page=${page}&size=${size}`),
  adminGet: (id) => request('GET', `/api/admin/posts/${encodeURIComponent(id)}`),
  adminCreate: (post) => request('POST', '/api/admin/posts', post),
  adminUpdate: (id, post) => request('PUT', `/api/admin/posts/${encodeURIComponent(id)}`, post),
  adminDelete: (id) => request('DELETE', `/api/admin/posts/${encodeURIComponent(id)}`),
};

/** 로그인 확인 겸 CSRF 쿠키 발급. 비로그인이면 null */
export async function currentUser() {
  try {
    return await api.me();
  } catch (e) {
    if (e instanceof ApiError && e.status === 401) return null;
    throw e;
  }
}
