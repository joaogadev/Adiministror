import { get, post, put, remove } from "./api";
export const salasService = {
  list: (id) => get(`/salas/galeria/${id}`),
  search: (nome) => get("/salas/buscar", { nome }),
  get: (id) => get(`/salas/${id}`),
  create: (id, data) => post(`/salas/galeria/${id}`, data),
  update: (id, data) => put(`/salas/${id}`, data),
  remove: (id) => remove(`/salas/${id}`),
};
