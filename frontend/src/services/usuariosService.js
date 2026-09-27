import { get } from "./api";
export const usuariosService = {
  list: () =>
    get("/usuario/usuarios").then((rows) => rows.map((row) => row.body ?? row)),
};
