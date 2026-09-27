import { get, post, put, remove } from "./api";
export const gastosExtrasService = {
  list: (id) => get(`/gastos-extras/galeria/${id}`),
  period: (id, inicio, fim) =>
    get(`/gastos-extras/galeria/${id}/periodo`, { inicio, fim }),
  create: (id, data) => post(`/gastos-extras/galeria/${id}`, data),
  update: (id, data) => put(`/gastos-extras/${id}`, data),
  remove: (id) => remove(`/gastos-extras/${id}`),
};
