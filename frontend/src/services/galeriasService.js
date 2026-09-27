import { get, post, put, remove } from "./api";
// TODO BACKEND: endpoint global de galerias. A busca administrativa é por nome.
// TODO BACKEND: GaleriaService.update ignora endereco; edição de endereço indisponível.
export const galeriasService = {
  list: () => get("/galeria/minhas"),
  search: (nome) => get("/galeria/buscar", { nome }),
  get: (id) => get(`/galeria/${id}`),
  count: (id) => get(`/galeria/${id}/salas/count`),
  create: (data) => post("/galeria", data),
  update: (id, data) => put(`/galeria/${id}`, data),
  remove: (id) => remove(`/galeria/${id}`),
};
