import { useState } from "react";
import { Plus, Check, Calendar, Eye } from "lucide-react";
import {
  PageHeader,
  State,
  Modal,
  Form,
  SearchBox,
  Badge,
  RecordList,
  Detail,
} from "../components/UI";
import { useData, useAction } from "../hooks/data";
import { pagamentosService } from "../services/pagamentosService";
import { alugueisService } from "../services/alugueisService";
import { money, date, month, today } from "../utils/format";
export default function Pagamentos() {
  const q = useData("pagamentos", pagamentosService.list);
  const rents = useData("alugueis", alugueisService.list);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [rental, setRental] = useState("");
  const [action, setAction] = useState(null);
  const [detail, setDetail] = useState(null);
  const save = useAction(
    (d) =>
      action.type === "create"
        ? pagamentosService.create(d.aluguelId, {
            competencia: d.competencia + "-01",
          })
        : action.type === "pay"
          ? pagamentosService.pay(action.item.id, {
              dataPagamento: d.dataPagamento,
            })
          : pagamentosService.due(action.item.id, {
              dataVencimento: d.dataVencimento,
            }),
    ["pagamentos"],
  );
  const name = (id) => {
    const r = rents.data?.find((r) => r.id === id);
    return r ? `${r.salaNome} · ${r.tenantNome}` : id;
  };
  const items =
    q.data?.filter(
      (p) =>
        (!status || p.status === status) &&
        (!rental || p.aluguelId === rental) &&
        `${name(p.aluguelId)} ${month(p.competencia)}`
          .toLowerCase()
          .includes(search.toLowerCase()),
    ) || [];
  return (
    <>
      <PageHeader
        title="Finanças com mais clareza."
        description="Cada recebimento no lugar certo. Acompanhe suas mensalidades."
        action={
          <button className="btn" onClick={() => setAction({ type: "create" })}>
            <Plus size={18} /> Gerar mensalidade
          </button>
        }
      />
      <div className="tabs">
        {[
          ["", "Todos"],
          ["PENDENTE", "Pendentes"],
          ["ATRASADO", "Atrasados"],
          ["PAGO", "Pagos"],
        ].map(([v, l]) => (
          <button
            key={v}
            className={status === v ? "active" : ""}
            onClick={() => setStatus(v)}
          >
            {l}
          </button>
        ))}
      </div>
      <div className="toolbar">
        <SearchBox
          value={search}
          onChange={setSearch}
          placeholder="Buscar sala, inquilino ou competência"
        />
        <select
          aria-label="Filtrar aluguel"
          value={rental}
          onChange={(e) => setRental(e.target.value)}
        >
          <option value="">Todos os aluguéis</option>
          {rents.data?.map((r) => (
            <option key={r.id} value={r.id}>
              {r.salaNome} · {r.tenantNome}
            </option>
          ))}
        </select>
      </div>
      <State query={q} empty="Nenhuma mensalidade encontrada.">
        <section className="panel table-panel">
          <RecordList
            items={items}
            columns={[
              {
                label: "Aluguel",
                render: (p) => (
                  <span className="break-word">{name(p.aluguelId)}</span>
                ),
              },
              { label: "Competência", render: (p) => month(p.competencia) },
              {
                label: "Valor",
                render: (p) => <strong>{money(p.valor)}</strong>,
              },
              { label: "Vencimento", render: (p) => date(p.dataVencimento) },
              { label: "Status", render: (p) => <Badge value={p.status} /> },
            ]}
            actions={(p) => (
              <>
                <button
                  className="icon-btn"
                  aria-label="Ver pagamento"
                  onClick={() => setDetail(p)}
                >
                  <Eye size={16} />
                </button>
                {p.status !== "PAGO" && (
                  <>
                    <button
                      className="icon-btn"
                      aria-label="Registrar pagamento"
                      onClick={() => setAction({ type: "pay", item: p })}
                    >
                      <Check size={16} />
                    </button>
                    <button
                      className="icon-btn"
                      aria-label="Alterar vencimento"
                      onClick={() => setAction({ type: "due", item: p })}
                    >
                      <Calendar size={16} />
                    </button>
                  </>
                )}
              </>
            )}
          />
        </section>
      </State>
      {action && (
        <Modal
          title={
            action.type === "create"
              ? "Gerar mensalidade"
              : action.type === "pay"
                ? "Registrar pagamento"
                : "Alterar vencimento"
          }
          onClose={() => setAction(null)}
        >
          {action.type === "create" && !rents.isSuccess ? (
            <State query={rents} />
          ) : (
            <Form
              fields={
                action.type === "create"
                  ? [
                      {
                        name: "aluguelId",
                        label: "Aluguel",
                        options:
                          rents.data
                            ?.filter((r) => r.status === "ATIVO")
                            .map((r) => ({
                              value: r.id,
                              label: `${r.salaNome} · ${r.tenantNome}`,
                            })) || [],
                      },
                      {
                        name: "competencia",
                        label: "Competência",
                        type: "month",
                      },
                    ]
                  : [
                      {
                        name:
                          action.type === "pay"
                            ? "dataPagamento"
                            : "dataVencimento",
                        label:
                          action.type === "pay"
                            ? "Data do pagamento"
                            : "Novo vencimento",
                        type: "date",
                      },
                    ]
              }
              defaults={{
                dataPagamento: today(),
                dataVencimento: action.item?.dataVencimento,
                competencia: today().slice(0, 7),
              }}
              onClose={() => setAction(null)}
              onSubmit={async (d) => {
                await save.mutateAsync(d);
                setAction(null);
              }}
            />
          )}
        </Modal>
      )}
      {detail && (
        <Modal title="Detalhes do pagamento" onClose={() => setDetail(null)}>
          <Detail
            data={{
              Aluguel: name(detail.aluguelId),
              Competência: month(detail.competencia),
              Valor: money(detail.valor),
              Vencimento: date(detail.dataVencimento),
              Pagamento: date(detail.dataPagamento),
              Status: detail.status,
            }}
          />
        </Modal>
      )}
    </>
  );
}
