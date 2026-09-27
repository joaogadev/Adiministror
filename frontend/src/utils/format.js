export const money = (n) =>
  new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(
    Number(n) || 0,
  );
export const date = (s) =>
  s ? new Date(s + "T12:00:00").toLocaleDateString("pt-BR") : "—";
export const month = (s) =>
  s
    ? new Date(s + "T12:00:00").toLocaleDateString("pt-BR", {
        month: "long",
        year: "numeric",
      })
    : "—";
export const today = () => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
};
export const digits = (s) => (s || "").replace(/\D/g, "");
export const phone = (s) => (s || "").replace(/[^+\d]/g, "");
