import { useState } from "react";
import { useForm } from "react-hook-form";
import { Plus, ArrowRight, Check, Pencil, Eye, Square } from "lucide-react";
import { toast } from "sonner";
import {
  PageHeader,
  State,
  Modal,
  Form,
  Field,
  Confirm,
  SearchBox,
  Badge,
  RecordList,
  Detail,
} from "../components/UI";
import GalleryPicker from "../components/GalleryPicker";
import { useData, useAction } from "../hooks/data";
import { alugueisService } from "../services/alugueisService";
import { salasService } from "../services/salasService";
import { tenantsService } from "../services/tenantsService";
import { pagamentosService } from "../services/pagamentosService";
import { contratosService } from "../services/contratosService";
import { occupied } from "../adapters/entities";
import { date, money, today } from "../utils/format";
import { message } from "../services/api";
export const tenantFields = [
  { name: "name", label: "Nome completo", maxLength: 100 },
  {
    name: "phone",
    label: "Telefone",
    pattern: /^\+?[0-9]{10,15}$/,
    placeholder: "79999999999",
  },
  { name: "email", label: "E-mail", type: "email", maxLength: 255 },
  {
    name: "documentType",
    label: "Tipo do documento",
    options: [
      { value: "CPF", label: "CPF" },
      { value: "CNPJ", label: "CNPJ" },
    ],
  },
  {
    name: "documentNumber",
    label: "Documento (somente números)",
    pattern: /^[0-9]+$/,
  },
];
const rentalFields = [
  { name: "dataInicio", label: "Data de início", type: "date" },
  {
    name: "diaVencimentoPadrao",
    label: "Dia do vencimento",
    type: "number",
    min: 1,
    max: 31,
  },
  {
    name: "valorAluguel",
    label: "Valor mensal (R$)",
    type: "number",
    min: 0.01,
    step: 0.01,
  },
];
export function RentalHistory({ rental }) {
  const payments = useData(["pagamentos", "aluguel", rental.id], () =>
    pagamentosService.rental(rental.id),
  );
  const contracts = useData(["contratos", "aluguel", rental.id], () =>
    contratosService.rental(rental.id),
  );
  return (
    <>
      <Detail
        data={{
          Sala: rental.salaNome,
          Inquilino: rental.tenantNome,
          Início: date(rental.dataInicio),
          "Dia do vencimento": rental.diaVencimentoPadrao,
          Status: rental.status,
        }}
      />
      <h3>Mensalidades</h3>
      <State query={payments}>
        <RecordList
          items={payments.data || []}
          columns={[
            { label: "Competência", render: (p) => date(p.competencia) },
            { label: "Valor", render: (p) => money(p.valor) },
            { label: "Status", render: (p) => <Badge value={p.status} /> },
          ]}
        />
      </State>
      <h3>Histórico de contratos</h3>
      <State query={contracts}>
        <RecordList
          items={contracts.data || []}
          columns={[
            { label: "Início", render: (c) => date(c.dataInicio) },
            { label: "Fim", render: (c) => date(c.dataFim) },
            { label: "Status", render: (c) => <Badge value={c.status} /> },
          ]}
        />
      </State>
    </>
  );
}
function RentalWizard({ onClose }) {
  const [step, setStep] = useState(0);
  const [gallery, setGallery] = useState("");
  const [sala, setSala] = useState("");
  const [lookup, setLookup] = useState(false);
  const [error, setError] = useState("");
  const [completed, setCompleted] = useState(false);
  const form = useForm({
    defaultValues: {
      dataInicio: today(),
      diaVencimentoPadrao: 10,
      contrato: { avisoAntecedenciaDias: 30 },
      inquilino: { documentType: "CPF" },
    },
  });
  const rooms = useData(
    ["salas", gallery],
    () => salasService.list(gallery),
    !!gallery,
  );
  const rents = useData("alugueis", alugueisService.list);
  const save = useAction(
    (d) => alugueisService.create(sala, d),
    ["alugueis", "salas", "tenants", "contratos", "pagamentos"],
    "Aluguel criado com sucesso.",
  );
  const steps = [
    "Galeria",
    "Sala",
    "Inquilino",
    "Aluguel",
    "Contrato",
    "Resumo",
  ];
  const grouped = [
    [],
    [],
    tenantFields.map((f) => ({ ...f, name: "inquilino." + f.name })),
    rentalFields,
    [
      {
        name: "contrato.dataFim",
        label: "Fim do contrato",
        type: "date",
        validate: (v) =>
          v > form.getValues("dataInicio") ||
          "A data final deve ser posterior à inicial.",
      },
      {
        name: "contrato.avisoAntecedenciaDias",
        label: "Avisar com antecedência (dias)",
        type: "number",
        min: 1,
      },
    ],
    [],
  ];
  const next = async () => {
    if (step === 0 && !gallery) return setError("Selecione uma galeria.");
    if (step === 1 && (!sala || !rents.isSuccess))
      return setError("Selecione uma sala disponível.");
    if (!(await form.trigger(grouped[step].map((f) => f.name)))) return;
    setError("");
    setStep(step + 1);
  };
  const submit = async () => {
    try {
      await save.mutateAsync(form.getValues());
      setCompleted(true);
    } catch (e) {
      Object.entries(e.response?.data?.validationErrors || {}).forEach(
        ([k, v]) => form.setError(k, { message: v }),
      );
      setError(message(e));
      setStep(2);
    }
  };
  if (completed)
    return (
      <Modal title="Aluguel criado com sucesso" onClose={onClose}>
        <div className="success-mark">
          <Check size={30} />
        </div>
        {[
          "Inquilino vinculado",
          "Contrato criado",
          "Primeira mensalidade gerada",
        ].map((t) => (
          <p className="success-line" key={t}>
            <Check size={18} />
            {t}
          </p>
        ))}
        <button className="btn" onClick={onClose}>
          Voltar aos aluguéis
        </button>
      </Modal>
    );
  return (
    <Modal title="Novo aluguel" onClose={onClose}>
      <div className="wizard-progress">
        {steps.map((s, i) => (
          <span
            className={i === step ? "current" : i < step ? "done" : ""}
            key={s}
          >
            <b>{i < step ? "✓" : i + 1}</b>
            {s}
          </span>
        ))}
      </div>
      <h3>{steps[step]}</h3>
      {step === 0 && (
        <GalleryPicker
          value={gallery}
          onChange={(id) => {
            setGallery(id);
            setSala("");
          }}
        />
      )}
      {step === 1 && (
        <State query={rooms}>
          <State query={rents}>
            <div className="room-options">
              {rooms.data
                ?.filter((r) => !occupied(r.id, rents.data || []))
                .map((r) => (
                  <button
                    className={
                      "room-option " + (sala === r.id ? "selected" : "")
                    }
                    key={r.id}
                    onClick={() => setSala(r.id)}
                  >
                    {r.nome}
                    <Badge value="DISPONÍVEL" />
                  </button>
                ))}
              {rooms.data?.every((r) => occupied(r.id, rents.data || [])) && (
                <p>Nenhuma sala disponível nesta galeria.</p>
              )}
            </div>
          </State>
        </State>
      )}
      {step >= 2 && step <= 4 && (
        <div className="form-grid">
          {grouped[step].map((field) => (
            <Field key={field.name} field={field} form={form} />
          ))}
        </div>
      )}
      {step === 2 && (
        <>
          <button
            className="btn secondary lookup"
            disabled={lookup}
            onClick={async () => {
              const n = form.getValues("inquilino.documentNumber");
              if (!n || !/^\d+$/.test(n)) {
                setError("Informe o documento com números.");
                return;
              }
              setLookup(true);
              setError("");
              try {
                const t = await tenantsService.document(n);
                form.setValue("inquilino", {
                  name: t.nome,
                  phone: t.phone,
                  email: t.email,
                  documentType: t.documentType,
                  documentNumber: t.documentNumber,
                });
                toast.success("Inquilino encontrado. Dados preenchidos.");
              } catch (e) {
                if (e.response?.status === 404)
                  toast.info(
                    "Documento não encontrado. Preencha os dados do novo inquilino.",
                  );
                else setError(message(e));
              } finally {
                setLookup(false);
              }
            }}
          >
            {lookup ? "Consultando…" : "Consultar documento existente"}
          </button>
          <p className="muted">
            O inquilino será criado ou reaproveitado ao confirmar o aluguel.
          </p>
        </>
      )}
      {step === 5 && (
        <>
          <Detail
            data={{
              Sala: rooms.data?.find((r) => r.id === sala)?.nome,
              Inquilino: form.getValues("inquilino.name"),
              Documento: form.getValues("inquilino.documentNumber"),
              Email: form.getValues("inquilino.email"),
              Telefone: form.getValues("inquilino.phone"),
              Início: date(form.getValues("dataInicio")),
              "Dia do vencimento": form.getValues("diaVencimentoPadrao"),
              Mensalidade: money(form.getValues("valorAluguel")),
              "Fim do contrato": date(form.getValues("contrato.dataFim")),
              "Antecedência do aviso": form.getValues(
                "contrato.avisoAntecedenciaDias",
              ),
            }}
          />
          <div className="notice">
            Uma confirmação cria o aluguel, o contrato e a primeira mensalidade.
          </div>
        </>
      )}
      {error && (
        <p className="field-error" role="alert">
          {error}
        </p>
      )}
      <div className="form-actions">
        <button
          className="btn secondary"
          disabled={save.isPending}
          onClick={() => (step ? setStep(step - 1) : onClose())}
        >
          {step ? "Voltar" : "Cancelar"}
        </button>
        {step < 5 ? (
          <button className="btn" onClick={next}>
            Continuar <ArrowRight size={16} />
          </button>
        ) : (
          <button className="btn" disabled={save.isPending} onClick={submit}>
            {save.isPending ? "Criando…" : "Confirmar aluguel"}
          </button>
        )}
      </div>
    </Modal>
  );
}
export default function Alugueis() {
  const q = useData("alugueis", alugueisService.list);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [create, setCreate] = useState(false);
  const [edit, setEdit] = useState(null);
  const [detail, setDetail] = useState(null);
  const [close, setClose] = useState(null);
  const update = useAction(
    (d) => alugueisService.update(edit.id, d),
    ["alugueis"],
  );
  const end = useAction(
    (id) => alugueisService.close(id),
    ["alugueis", "contratos", "salas", "tenants"],
    "Aluguel encerrado. Histórico preservado.",
  );
  const items =
    q.data?.filter(
      (r) =>
        (!status || r.status === status) &&
        `${r.salaNome} ${r.tenantNome}`
          .toLowerCase()
          .includes(search.toLowerCase()),
    ) || [];
  return (
    <>
      <PageHeader
        title="Relações que ocupam seus espaços."
        description="Acompanhe os aluguéis do início ao próximo capítulo."
        action={
          <button className="btn" onClick={() => setCreate(true)}>
            <Plus size={18} /> Novo aluguel
          </button>
        }
      />
      <div className="toolbar">
        <SearchBox
          value={search}
          onChange={setSearch}
          placeholder="Buscar sala ou inquilino"
        />
        <select
          aria-label="Status do aluguel"
          value={status}
          onChange={(e) => setStatus(e.target.value)}
        >
          <option value="">Todos os status</option>
          <option>ATIVO</option>
          <option>ENCERRADO</option>
        </select>
      </div>
      <State query={q} empty="Nenhum aluguel encontrado.">
        <section className="panel table-panel">
          <RecordList
            items={items}
            columns={[
              { label: "Sala", key: "salaNome" },
              { label: "Inquilino", key: "tenantNome" },
              { label: "Início", render: (r) => date(r.dataInicio) },
              {
                label: "Vencimento",
                render: (r) => `Dia ${r.diaVencimentoPadrao}`,
              },
              { label: "Status", render: (r) => <Badge value={r.status} /> },
            ]}
            actions={(r) => (
              <>
                <button
                  className="icon-btn"
                  aria-label={"Visualizar aluguel " + r.salaNome}
                  onClick={() => setDetail(r)}
                >
                  <Eye size={16} />
                </button>
                {r.status === "ATIVO" && (
                  <>
                    <button
                      className="icon-btn"
                      aria-label={"Editar aluguel " + r.salaNome}
                      onClick={() => setEdit(r)}
                    >
                      <Pencil size={16} />
                    </button>
                    <button
                      className="icon-btn"
                      aria-label={"Encerrar aluguel " + r.salaNome}
                      onClick={() => setClose(r)}
                    >
                      <Square size={15} />
                    </button>
                  </>
                )}
              </>
            )}
          />
        </section>
      </State>
      {create && <RentalWizard onClose={() => setCreate(false)} />}
      {edit && (
        <Modal title="Editar aluguel" onClose={() => setEdit(null)}>
          <div className="notice">
            Informe o valor mensal desejado. O valor atual não está disponível
            para consulta.
          </div>
          <Form
            defaults={edit}
            fields={rentalFields}
            onClose={() => setEdit(null)}
            onSubmit={async (d) => {
              await update.mutateAsync({
                dataInicio: d.dataInicio,
                diaVencimentoPadrao: d.diaVencimentoPadrao,
                valorAluguel: d.valorAluguel,
              });
              setEdit(null);
            }}
          />
        </Modal>
      )}
      {detail && (
        <Modal title="Detalhes do aluguel" onClose={() => setDetail(null)}>
          <RentalHistory rental={detail} />
        </Modal>
      )}
      {close && (
        <Confirm
          title="Encerrar aluguel"
          text="O aluguel e seu contrato ativo serão encerrados. O histórico será preservado; o inquilino poderá ser desativado se não tiver outro aluguel ativo."
          onClose={() => setClose(null)}
          pending={end.isPending}
          onConfirm={() =>
            end.mutate(close.id, { onSuccess: () => setClose(null) })
          }
        />
      )}
    </>
  );
}
