# Blog

개인 블로그. 프론트/백엔드 완전 분리 구조.

| 구분 | 스택 |
|---|---|
| 프론트 | React 19 + Vite 8, **MPA** (페이지별 HTML 엔트리) |
| 백엔드 | Java 21, Spring Boot 3.5, Spring Security, JPA, Flyway |
| DB | OCI Autonomous Database (Always Free), Wallet 접속 |
| 운영 | OCI Always Free VM (Ubuntu) + Nginx, 관리 기능은 Tailscale 내부 전용 |

## 구조

```
blog/
├── backend/            Spring Boot API 서버 (/api/**)
├── frontend/           React MPA (정적 파일로 빌드 → Nginx 서빙)
│   ├── index.html          글 목록      → src/pages/home.jsx
│   ├── post.html?id=N      글 상세      → src/pages/post.jsx
│   ├── about.html          소개         → src/pages/about.jsx
│   └── admin/              관리자(로그인·작성·수정·삭제)
├── deploy/             Nginx / systemd / 환경변수 샘플
└── .github/workflows/  CI (백엔드 테스트·빌드, 프론트 빌드)
```

## 로컬 실행

**백엔드** — 기본 `local` 프로필은 H2 인메모리(Oracle 모드)라 Wallet 없이 바로 실행됩니다.

```bash
cd backend
./gradlew bootRun
# http://localhost:8080/api/posts
```

로컬 관리자 계정: `admin` / `admin1234` (local 프로필 전용)

**프론트**

```bash
cd frontend
npm install
npm run dev
# http://localhost:5173  (/api 는 8080으로 프록시)
# 관리자: http://localhost:5173/admin/
```

## API

| 메서드 | 경로 | 권한 |
|---|---|---|
| GET | `/api/posts?page=&size=` | 공개 (발행글만) |
| GET | `/api/posts/{id}` | 공개 (발행글만) |
| GET | `/api/auth/me` | 공개 (로그인 확인 + CSRF 쿠키 발급) |
| POST | `/api/auth/login`, `/api/auth/logout` | 공개 (CSRF 필요) |
| GET/POST/PUT/DELETE | `/api/admin/posts[/{id}]` | ADMIN (세션 + CSRF) |

## 보안 설계

- 백엔드는 `127.0.0.1:8080`에만 바인딩 → 외부에서는 Nginx를 통해서만 접근
- 관리자 화면·로그인·관리 API는 Nginx에서 **Tailscale 대역(100.64.0.0/10)만 허용** + 앱 로그인 이중 보호
- 세션 쿠키 `HttpOnly; Secure; SameSite=Strict`, CSRF는 `XSRF-TOKEN` 쿠키 ↔ `X-XSRF-TOKEN` 헤더
- 정의되지 않은 경로는 Spring Security에서 전부 `denyAll`, 공개 API는 Nginx에서 GET만 허용
- 로그인 rate limit (Nginx `5r/m`), 응답에 스택트레이스/메시지 노출 안 함
- 마크다운 본문은 렌더링 시 항상 DOMPurify로 정화, CSP `script-src 'self'`
- DB 비밀번호·Wallet·관리자 해시는 저장소 밖 `/etc/blog/backend.env`, `/opt/blog/wallet` (`.gitignore`로 차단)

## 운영 배포 (요약)

1. OCI Autonomous DB 생성 → 앱 전용 사용자(`BLOG_APP`) 생성 → Wallet 다운로드
2. VM에 Wallet 압축 해제: `/opt/blog/wallet` (소유자 `blog`, 권한 700)
3. `deploy/backend.env.example` → `/etc/blog/backend.env` 로 복사 후 값 입력 (권한 600)
4. `./gradlew bootJar` 결과물을 `/opt/blog/blog-backend.jar` 로 배치, `deploy/systemd/blog-backend.service` 등록
5. `npm run build` 결과물 `frontend/dist/` → `/var/www/blog`
6. `deploy/nginx/*.conf` 적용 (`blog-proxy.conf`는 `/etc/nginx/snippets/`)
7. 테이블은 백엔드 기동 시 Flyway가 자동 생성 (`backend/src/main/resources/db/migration`)

HTTPS 적용 전 HTTP로 테스트할 때는 `COOKIE_SECURE=false`로 두어야 로그인이 됩니다.
