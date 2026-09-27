import { api } from "./api";
export const authService = {
  login: (data) =>
    api.post("/usuario/login", data, { public: true }).then((r) => r.data),
  register: (data) =>
    api.post("/usuario/registro", data, { public: true }).then((r) => r.data),
};
