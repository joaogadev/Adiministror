// Fixtures exclusivas dos testes. Nunca importadas pela aplicação.
export const user = {
  id: "11111111-1111-4111-8111-111111111111",
  nome: "Marina Costa",
  email: "marina@example.test",
  phone: "79999999999",
  role: "DONO",
};
export const gallery = {
  id: "22222222-2222-4222-8222-222222222222",
  nome: "Galeria Jardim",
  phone: "79999999999",
  endereco: {
    zipCode: "49000000",
    estado: "SE",
    cidade: "Aracaju",
    bairro: "Centro",
    rua: "Rua das Palmeiras",
    numero: "120",
    complemento: "",
  },
  dono: user,
};
export const room = {
  id: "33333333-3333-4333-8333-333333333333",
  nome: "Sala 01",
  tenantId: null,
  tenantNome: null,
};
export const tenant = {
  id: "44444444-4444-4444-8444-444444444444",
  nome: "Ana Oliveira",
  email: "ana@example.test",
  phone: "79999999999",
  documentType: "CPF",
  documentNumber: "12345678900",
};
export const rental = {
  id: "55555555-5555-4555-8555-555555555555",
  salaId: room.id,
  salaNome: room.nome,
  tenantId: tenant.id,
  tenantNome: tenant.nome,
  dataInicio: "2026-01-01",
  dataVencimento: "10",
  status: "ATIVO",
};
export const contract = {
  salaNome: room.nome,
  inquilinoNome: tenant.nome,
  proprietarioNome: user.nome,
  dataInicio: "2026-01-01",
  dataFim: "2027-01-01",
  avisoAntecedenciaDias: 30,
  dataAviso: "2026-12-02",
  status: "ATIVO",
};
export const payments = Array.from({ length: 12 }, (_, i) => ({
  id: "payment-" + i,
  aluguelID: rental.id,
  competencia: `2026-${String(1 + Math.floor(i / 2)).padStart(2, "0")}-01`,
  valor: 850 + i * 30,
  dataVencimento: "2026-09-10",
  dataPagamento: i % 3 === 0 ? "2026-09-05" : null,
  status: ["PAGO", "PENDENTE", "ATRASADO"][i % 3],
}));
export const token = (role = "DONO") =>
  [
    Buffer.from("{}").toString("base64url"),
    Buffer.from(
      JSON.stringify({
        sub: user.id,
        email: user.email,
        role: [role],
        exp: Math.floor(Date.now() / 1000) + 3600,
      }),
    ).toString("base64url"),
    "test",
  ].join(".");
export async function setup(page, role = "DONO", { tour = false } = {}) {
  await page.addInitScript(
    ({ token, id, role, tour }) => {
      localStorage.setItem("adiministror_token", token);
      if (!tour)
        localStorage.setItem(`adiministror_onboarding_${id}_${role}`, "true");
    },
    { token: token(role), id: user.id, role, tour },
  );
  return mockApi(page, role);
}
export async function mockApi(page, role = "DONO") {
  const writes = [];
  await page.route("**/api/**", async (route) => {
    const req = route.request();
    const url = new URL(req.url());
    const path = url.pathname.replace("/api", "");
    if (req.method() !== "GET") {
      writes.push({
        path,
        method: req.method(),
        data: req.postDataJSON(),
        auth: req.headers().authorization,
      });
      return route.fulfill({
        json:
          path === "/usuario/login"
            ? { accessToken: token(role) }
            : path.startsWith("/alugueis/sala")
              ? rental
              : {},
      });
    }
    let data;
    if (path === "/galeria/minhas" || path === "/galeria/buscar")
      data = [gallery];
    else if (path === `/galeria/${gallery.id}`) data = gallery;
    else if (path.endsWith("/salas/count")) data = 2;
    else if (path === `/salas/galeria/${gallery.id}`)
      data = [
        room,
        {
          ...room,
          id: "66666666-6666-4666-8666-666666666666",
          nome: "Sala 02",
        },
      ];
    else if (path === "/alugueis" || path.startsWith("/alugueis/tenant/"))
      data = [rental];
    else if (path === "/tenants") data = [tenant, tenant];
    else if (path.startsWith("/tenants/documento/")) data = tenant;
    else if (path === "/pagamento" || path.startsWith("/pagamento/aluguel/"))
      data = payments;
    else if (path === "/contratos" || path.startsWith("/contratos/aluguel/"))
      data = [contract];
    else if (path === "/contratos/proximo-vencimento") data = [contract];
    else if (path.startsWith("/gastos-extras/galeria/"))
      data = [
        {
          id: "expense-1",
          nome: "Manutenção elétrica",
          descricao: "Troca de lâmpadas",
          valor: 250,
          dataGasto: "2026-09-01",
          nomeGaleria: gallery.nome,
        },
      ];
    else if (path === "/usuario/usuarios")
      data = [
        { body: { ...user, role }, statusCode: "OK", statusCodeValue: 200 },
      ];
    else throw new Error("Endpoint não mapeado: " + path);
    return route.fulfill({ json: data });
  });
  return writes;
}
