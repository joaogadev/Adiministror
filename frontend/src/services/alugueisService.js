import { get, post, put, patch } from "./api";
import { aluguelAdapter } from "../adapters/entities";
// TODO BACKEND: AluguelResponse não informa valorAluguel; edição exige informar valor.
export const alugueisService = {
  list: () => get("/alugueis").then((a) => a.map(aluguelAdapter)),
  get: (id) => get(`/alugueis/${id}`).then(aluguelAdapter),
  tenant: (id) =>
    get(`/alugueis/tenant/${id}`).then((a) => a.map(aluguelAdapter)),
  create: (id, data) => post(`/alugueis/sala/${id}`, data),
  update: (id, data) => put(`/alugueis/${id}`, data),
  close: (id) => patch(`/alugueis/${id}/encerrar`),
};
