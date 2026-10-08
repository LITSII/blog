import { Layout, mount } from '../shared/Layout.jsx';

function About() {
  return (
    <Layout>
      <h1>소개</h1>
      <div className="prose">
        <p>웹 개발자의 개인 블로그입니다. 개발하면서 배운 것과 생각을 기록합니다.</p>
        <p>이 페이지 내용은 <code>frontend/src/pages/about.jsx</code>에서 수정하세요.</p>
      </div>
    </Layout>
  );
}

mount(<About />);
