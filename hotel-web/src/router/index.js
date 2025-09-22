import { createRouter, createWebHistory } from "vue-router";
// Auth components
import Login from "@/components/auth/Login.vue";
import Register from "@/components/auth/Register.vue";
import ForgotPassword from "@/components/auth/ForgotPassword.vue";
import LoginVerify from "@/components/auth/LoginVerify.vue";
import PasswordReset from "@/components/auth/PasswordReset.vue";
import OAuth2Redirect from "@/components/auth/OAuth2Redirect.vue";
// Page components
import MainPage from "@/components/page/MainPage.vue";
import TermsPage from "@/components/page/Terms.vue";
import PrivacyPage from "@/components/page/Privacy.vue";
import Ownerpage from "@/components/Owner/Ownerpage.vue";

const routes = [
  { path: "/", component: MainPage }, // 기본 경로를 MainPage로 설정
  { path: "/login", component: Login },
  { path: "/register", component: Register },
  { path: "/terms", component: TermsPage },
  { path: "/privacy", component: PrivacyPage },
  { path: "/forgotPassword", component: ForgotPassword },
  { path: "/forgot-password", component: ForgotPassword }, // 추가 경로
  { path: "/verify", component: LoginVerify },
  { path: "/passwordReset", component: PasswordReset },
  { path: "/password-reset", component: PasswordReset }, // 추가 경로
  { path: "/oauth2/redirect", component: OAuth2Redirect },
  { path: "/owner", component: Ownerpage, meta: { requiresAuth: true } }, // 업주 페이지(로그인 인증)
];
const router = createRouter({
  history: createWebHistory(),
  routes,
});

// ✨ 네비게이션 가드 추가 ✨
// router.beforeEach는 모든 라우팅 요청이 처리되기 전에 실행됩니다.
router.beforeEach((to, from, next) => {
  // 이동하려는 페이지(to)가 인증을 필요로 하는지 확인합니다.
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  
  // localStorage에 토큰이 있는지 확인하여 로그인 상태를 파악합니다.
  const isLoggedIn = !!localStorage.getItem('token');

  // 인증이 필요한 페이지에 접근하려는데, 로그인이 되어있지 않다면
  if (requiresAuth && !isLoggedIn) {
    alert('로그인이 필요한 페이지입니다.');
    // 로그인 페이지로 강제 이동시킵니다.
    next('/login');
  } else {
    // 그 외의 경우는 정상적으로 페이지 이동을 허용합니다.
    next();
  }
});

export default router;