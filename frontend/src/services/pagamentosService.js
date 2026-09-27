import { get, post, patch } from "./api";
import { pagamentoAdapter } from "../adapters/entities";
export const pagamentosService = {
  list: () => get("/pagamento").then((a) => a.map(pagamentoAdapter)),
  rental: (id) =>
    get(`/pagamento/aluguel/${id}`).then((a) => a.map(pagamentoAdapter)),
  create: (id, data) => post(`/pagamento/aluguel/${id}`, data),
  pay: (id, data) => patch(`/pagamento/${id}/pagar`, data),
  due: (id, data) => patch(`/pagamento/${id}/vencimento`, data),
};
