import { test, expect } from "@playwright/test";
import { setup, mockApi, gallery } from "./fixtures";
import fs from "node:fs";
test("capturas desktop, mobile, dark e navegação móvel", async ({ page }) => {
  await setup(page);
  fs.mkdirSync("docs/previews", { recursive: true });
  await page.setViewportSize({ width: 1440, height: 1000 });
  await page.goto("/");
  await expect(
    page.getByRole("heading", { name: "Seu negócio em perspectiva." }),
  ).toBeVisible();
  await page.waitForTimeout(1000);
  await page.screenshot({
    path: "docs/previews/dashboard-desktop.png",
    fullPage: true,
  });
  await page.goto("/galerias");
  await expect(
    page.getByRole("heading", { name: "Galeria Jardim" }),
  ).toBeVisible();
  await page.screenshot({
    path: "docs/previews/galerias-desktop.png",
    fullPage: true,
  });
  await page.goto("/configuracoes");
  await page.getByRole("button", { name: "Escuro", exact: true }).click();
  await page.goto("/");
  await page.waitForTimeout(700);
  await page.screenshot({
    path: "docs/previews/dashboard-dark.png",
    fullPage: true,
  });
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto("/pagamentos");
  await expect(page.getByText("Finanças com mais clareza.")).toBeVisible();
  await page.screenshot({
    path: "docs/previews/pagamentos-mobile-dark.png",
    fullPage: true,
  });
  await page.getByRole("button", { name: "Abrir menu" }).click();
  await page.getByRole("link", { name: "Configurações", exact: true }).click();
  await expect(page).toHaveURL(/configuracoes/);
  await page.getByRole("button", { name: "Claro", exact: true }).click();
  await page.goto("/");
  await page.waitForTimeout(500);
  await page.screenshot({
    path: "docs/previews/dashboard-mobile.png",
    fullPage: true,
  });
});
test("captura admin", async ({ page }) => {
  await setup(page, "ADMINISTRADOR");
  await page.setViewportSize({ width: 1440, height: 1000 });
  await page.goto("/");
  await page.waitForTimeout(900);
  await page.screenshot({
    path: "docs/previews/admin-desktop.png",
    fullPage: true,
  });
});
test("login funcional e visual", async ({ page }) => {
  await mockApi(page);
  await page.setViewportSize({ width: 1440, height: 1000 });
  await page.goto("/login");
  await page.screenshot({
    path: "docs/previews/login-desktop.png",
    fullPage: true,
  });
  await page.getByLabel("E-mail").fill("marina@example.test");
  await page.getByLabel("Senha", { exact: true }).fill("senha");
  await page.getByRole("button", { name: "Entrar no Adiministror" }).click();
  await expect(page).toHaveURL("/");
  await expect(
    page.getByRole("heading", { name: "Seu negócio em perspectiva." }),
  ).toBeVisible();
});
