export const environment = {
  production: true,

  // PROD senaryon:
  // A) Backend aynı domain altında reverse-proxy ile /api'ye yönleniyorsa:
  // apiBase: '/api',

  // B) Backend başka bir domain/port'ta ise ŞUNU yaz:
  // apiBase: 'https://SENIN_BACKEND_DOMAININ/api',

  apiBase: '/api',
};