import axios from 'axios';

const http = axios.create({
  baseURL: '/api',
  withCredentials: false,
  timeout: 10000,
});

// 요청 인터셉터: 모든 요청에 JWT 토큰을 자동으로 추가합니다.
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ✨ 응답 인터셉터: 401 오류를 전역적으로 처리합니다.
http.interceptors.response.use(
  (response) => {
    // 2xx 범위의 상태 코드는 이 함수를 트리거합니다.
    return response;
  },
  (error) => {
    // 2xx 외의 범위에 있는 상태 코드는 이 함수를 트리거합니다.
    if (error.response && error.response.status === 401) {
      
      // 로그인 실패(401) 시에는 리다이렉션을 방지합니다.
      if (!error.config.url.includes('/users/login')) {
        
        // 기존에 저장된 모든 인증 정보를 삭제합니다.
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        localStorage.removeItem('savedEmail');
        localStorage.removeItem('savedPassword');

        alert('세션이 만료되었거나 유효하지 않습니다. 다시 로그인해주세요.');
        
        // 로그인 페이지로 리다이렉트합니다. (페이지를 새로고침하여 상태를 초기화)
        window.location.href = '/login';
      }
    }
    // 다른 모든 오류는 그대로 반환하여 각 컴포넌트에서 처리할 수 있도록 합니다.
    return Promise.reject(error);
  }
);

export default http;