const fmt = new Intl.DateTimeFormat('ko-KR', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  timeZone: 'Asia/Seoul',
});

export function formatDate(iso) {
  return iso ? fmt.format(new Date(iso)) : '';
}

export function getParam(name) {
  return new URLSearchParams(window.location.search).get(name);
}
