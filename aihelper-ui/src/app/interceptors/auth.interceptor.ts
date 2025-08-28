import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token') ?? undefined;

  // login / register çağrılarına token ekleme
  const isLogin    = req.url.includes('/api/auth/login');
  const isRegister = req.url.includes('/api/auth/register');

  const shouldAttach = !!token && !(isLogin || isRegister);

  if (shouldAttach) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  console.log('[INT] url=', req.url, 'attachAuth=', shouldAttach);

  return next(req);
};