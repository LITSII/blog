import { marked } from 'marked';
import DOMPurify from 'dompurify';

marked.setOptions({ gfm: true, breaks: false });

/** 마크다운 → 정화된 HTML. 저장된 본문은 신뢰하지 않고 항상 DOMPurify를 거친다 */
export function renderMarkdown(md) {
  const html = marked.parse(md ?? '');
  return DOMPurify.sanitize(html, { USE_PROFILES: { html: true } });
}
