import { get, post } from "./api";
// TODO BACKEND: adicionar contratoId ao ContratoResponse para permitir renovação segura pela interface.
export const contratosService = {
  list: () => get("/contratos"),
  near: () => get("/contratos/proximo-vencimento"),
  rental: (id) => get(`/contratos/aluguel/${id}`),
  renew: (id, data) => {
    if (!id) throw new Error("Contrato sem identificador disponível.");
    return post(`/contratos/${id}/renovar`, data);
  },
};
