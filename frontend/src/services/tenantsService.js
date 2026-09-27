// TODO BACKEND: update altera somente nome, telefone e email; documento preservado.
import { get, put } from "./api";
import { unique } from "../adapters/entities";
export const tenantsService = {
  list: () => get("/tenants").then(unique),
  get: (id) => get(`/tenants/${id}`),
  document: (n) => get(`/tenants/documento/${encodeURIComponent(n)}`),
  update: (n, data) => put(`/tenants/documento/${encodeURIComponent(n)}`, data),
};
