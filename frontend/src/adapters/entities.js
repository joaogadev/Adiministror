export const aluguelAdapter = ({ dataVencimento, ...rest }) => ({
  ...rest,
  diaVencimentoPadrao: Number(dataVencimento),
});
export const pagamentoAdapter = ({ aluguelID, ...rest }) => ({
  ...rest,
  aluguelId: aluguelID,
});
export const unique = (items) =>
  Array.from(new Map(items.map((x) => [x.id, x])).values());
export const occupied = (salaId, alugueis) =>
  alugueis.some((a) => a.salaId === salaId && a.status === "ATIVO");
