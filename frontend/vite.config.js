import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { resolve } from 'node:path';

// MPA: 페이지마다 독립된 HTML 엔트리. 빌드 결과는 dist/ 아래 페이지별 HTML + 공용 청크
const page = (p) => resolve(import.meta.dirname, p);

export default defineConfig({
  plugins: [react()],
  build: {
    rollupOptions: {
      input: {
        index: page('index.html'),
        post: page('post.html'),
        about: page('about.html'),
        adminIndex: page('admin/index.html'),
        adminEdit: page('admin/edit.html'),
      },
    },
  },
  server: {
    // 개발 중에는 백엔드(8080)로 API 프록시 → 같은 오리진처럼 동작 (쿠키/CSRF 동일)
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
});
