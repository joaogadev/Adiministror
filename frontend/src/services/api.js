import axios from "axios";
export const TOKEN_KEY = "adiministror_token";
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
  timeout: 20000,
});
api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token && !config.public) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
api.interceptors.response.use(
  (r) => r,
  (error) => {
    if (error.response?.status === 401 && !error.config?.public) {
      localStorage.removeItem(TOKEN_KEY);
      window.dispatchEvent(new Event("session-expired"));
    }
    return Promise.reject(error);
  },
);
export const message = (e) =>
  e.response?.status === 403 && !e.config?.public
    ? "Você não possui permissão para realizar esta ação."
    : e.response?.data?.message ||
      (e.response
        ? "Não foi possível concluir a operação."
        : "Não foi possível conectar ao servidor. Verifique se o backend está em execução.");
export const get = (path, params) =>
  api.get(path, { params }).then((r) => r.data);
export const post = (path, data) => api.post(path, data).then((r) => r.data);
export const put = (path, data) => api.put(path, data).then((r) => r.data);
export const patch = (path, data) => api.patch(path, data).then((r) => r.data);
export const remove = (path) => api.delete(path).then((r) => r.data);
