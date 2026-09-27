import { test, expect } from "@playwright/test";
import { setup, mockApi, gallery, token, user } from "./fixtures";
const pages = [
  "/",
  "/galerias",
  "/galerias/" + gallery.id,
  "/salas",
  "/inquilinos",
  "/alugueis",
  "/pagamentos",
  "/contratos",
  "/gastos",
  "/assistente",
  "/configuracoes",
];
for (const role of ["DONO", "ADMINISTRADOR"])
  for (const width of [375, 390, 430, 768, 1024, 1440])
    test(`${role} navegação sem overflow em ${width}px`, async ({ page }) => {
      await page.setViewportSize({ width, height: 950 });
      await setup(page, role);
      const errors = [];
      page.on("pageerror", (e) => errors.push(e.message));
      for (const path of [
        ...pages,
        ...(role === "ADMINISTRADOR" ? ["/admin/usuarios"] : []),
      ]) {
        await page.goto(path);
        await expect(page.locator("h1")).toBeVisible();
        await page.waitForTimeout(100);
        expect(
          await page.evaluate(
            () => document.documentElement.scrollWidth <= innerWidth,
          ),
          path,
        ).toBe(true);
      }
      expect(errors).toEqual([]);
    });
test("wizard envia apenas uma criação e exclui sala ocupada", async ({
  page,
}) => {
  const writes = await setup(page);
  await page.goto("/alugueis");
  await page.getByRole("button", { name: "Novo aluguel", exact: true }).click();
  await page.locator("dialog select").selectOption(gallery.id);
  await page.getByRole("button", { name: "Continuar" }).click();
  await expect(
    page.getByRole("button", { name: "Sala 01 DISPONÍVEL" }),
  ).toHaveCount(0);
  await page.getByRole("button", { name: "Sala 02 DISPONÍVEL" }).click();
  await page.getByRole("button", { name: "Continuar" }).click();
  await page.getByLabel("Nome completo").fill("Ana Oliveira");
  await page.getByLabel("Telefone", { exact: true }).fill("79999999999");
  await page.getByLabel("E-mail", { exact: true }).fill("ana@example.test");
  await page.getByLabel("Documento (somente números)").fill("12345678900");
  await page.getByRole("button", { name: "Continuar" }).click();
  await page.getByLabel("Valor mensal (R$)").fill("850");
  await page.getByRole("button", { name: "Continuar" }).click();
  await page.getByLabel("Fim do contrato").fill("2028-09-25");
  await page.getByRole("button", { name: "Continuar" }).click();
  await page.getByRole("button", { name: "Confirmar aluguel" }).click();
  await expect(
    page.getByRole("heading", { name: "Aluguel criado com sucesso" }),
  ).toBeVisible();
  expect(writes).toHaveLength(1);
  expect(writes[0].path).toContain("/alugueis/sala/");
  expect(writes[0].data.valorAluguel).toBe(850);
  expect(writes[0].data.inquilino.name).toBe("Ana Oliveira");
  expect(writes[0].auth).toContain("Bearer ");
});
test("pagamento e vencimento usam payloads corretos", async ({ page }) => {
  const writes = await setup(page);
  await page.goto("/pagamentos");
  await page
    .getByRole("button", { name: "Registrar pagamento", exact: true })
    .first()
    .click();
  await page.getByLabel("Data do pagamento").fill("2026-09-25");
  await page.getByRole("button", { name: "Salvar", exact: true }).click();
  await expect(page.locator("dialog")).toHaveCount(0);
  expect(writes[0].method).toBe("PATCH");
  expect(writes[0].data).toEqual({ dataPagamento: "2026-09-25" });
  await page
    .getByRole("button", { name: "Alterar vencimento", exact: true })
    .first()
    .click();
  await page.getByLabel("Novo vencimento").fill("2026-10-10");
  await page.getByRole("button", { name: "Salvar", exact: true }).click();
  await expect(page.locator("dialog")).toHaveCount(0);
  expect(writes[1].data).toEqual({ dataVencimento: "2026-10-10" });
});
test("contrato sem id não permite renovação; dono não acessa usuários", async ({
  page,
}) => {
  await setup(page);
  await page.goto("/contratos");
  await expect(
    page.getByRole("button", { name: "Renovar contrato" }),
  ).toBeDisabled();
  await page.goto("/admin/usuarios");
  await expect(page).toHaveURL(/sem-permissao/);
});
test("401 limpa sessão e 403 mostra erro", async ({ page }) => {
  await setup(page);
  await page.route("**/api/pagamento", (r) =>
    r.fulfill({ status: 403, json: { message: "Acesso negado" } }),
  );
  await page.goto("/pagamentos");
  await expect(
    page.getByText("Você não possui permissão para realizar esta ação."),
  ).toBeVisible();
  await page.unroute("**/api/pagamento");
  await page.route("**/api/pagamento", (r) =>
    r.fulfill({ status: 401, json: {} }),
  );
  await page.reload();
  await expect(page).toHaveURL(/login/);
  expect(
    await page.evaluate(() => localStorage.getItem("adiministror_token")),
  ).toBeNull();
});
test("login e cadastro não enviam role; validação aparece junto ao campo", async ({
  page,
}) => {
  const writes = await mockApi(page);
  await page.goto("/registro");
  await page.getByLabel("Nome completo").fill("Marina Costa");
  await page.getByLabel("Telefone").fill("79999999999");
  await page.getByLabel("E-mail").fill("marina@example.test");
  await page.getByLabel("Senha", { exact: true }).fill("SenhaTeste123!");
  await page.getByRole("button", { name: "Criar minha conta" }).click();
  await expect(page).toHaveURL(/login/);
  expect(writes[0].data.role).toBeUndefined();
  await page.route("**/api/usuario/login", (r) =>
    r.fulfill({
      status: 400,
      json: {
        message: "Erro de validação",
        validationErrors: { email: "E-mail não aceito" },
      },
    }),
  );
  await page.getByLabel("E-mail").fill("marina@example.test");
  await page.getByLabel("Senha", { exact: true }).fill("senha");
  await page.getByRole("button", { name: "Entrar no Adiministror" }).click();
  await expect(page.getByText("E-mail não aceito")).toBeVisible();
});
for (const role of ["DONO", "ADMINISTRADOR"])
  test(`tutorial e dark mode ${role}`, async ({ page }) => {
    await setup(page, role, { tour: true });
    await page.goto("/");
    await expect(page.locator(".driver-popover")).toBeVisible();
    await expect(page.locator(".driver-popover-title")).toContainText(
      role === "DONO" ? "Boas-vindas" : "painel administrativo",
    );
    await page.locator(".driver-popover-close-btn").click();
    await page.goto("/configuracoes");
    await page.getByRole("button", { name: "Escuro", exact: true }).click();
    await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
    await page.reload();
    await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
    await page.getByRole("button", { name: "Refazer tutorial" }).click();
    await expect(page.locator(".driver-popover")).toBeVisible();
  });
