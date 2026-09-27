import { useState } from "react";
import { Eye, RefreshCw } from "lucide-react";
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
import { contratosService } from "../services/contratosService";
import { date } from "../utils/format";
export default function Contratos() {
  const [near, setNear] = useState(false);
  const q = useData(
    ["contratos", near ? "near" : "all"],
    near ? contratosService.near : contratosService.list,
  );
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [detail, setDetail] = useState(null);
  const [renew, setRenew] = useState(null);
  const save = useAction(
    (d) => contratosService.renew(renew.id, d),
    ["contratos"],
  );
  return (
    <>
      <PageHeader
        title="Compromissos bem acompanhados."
        description="Prazos, períodos e histórico dos contratos dos seus espaços."
      />
      <div className="notice">
        A renovação estará disponível quando os contratos trouxerem seu
        identificador. O histórico continua disponível para consulta.
      </div>
      <div className="toolbar">
        <SearchBox
          value={search}
          onChange={setSearch}
          placeholder="Buscar sala, inquilino ou proprietário"
        />
        <select
          aria-label="Status do contrato"
          value={status}
          onChange={(e) => setStatus(e.target.value)}
        >
          <option value="">Todos os status</option>
          <option>ATIVO</option>
          <option>RENOVADO</option>
          <option>ENCERRADO</option>
        </select>
        <button
          className={"btn " + (near ? "" : "secondary")}
          onClick={() => setNear(!near)}
        >
          Próximos do vencimento
        </button>
      </div>
      <State query={q}>
        <section className="panel table-panel">
          <RecordList
            items={
              q.data?.filter(
                (c) =>
                  (!status || c.status === status) &&
                  `${c.salaNome} ${c.inquilinoNome} ${c.proprietarioNome}`
                    .toLowerCase()
                    .includes(search.toLowerCase()),
              ) || []
            }
            columns={[
              { label: "Sala", key: "salaNome" },
              { label: "Inquilino", key: "inquilinoNome" },
              { label: "Início", render: (c) => date(c.dataInicio) },
              { label: "Fim", render: (c) => date(c.dataFim) },
              { label: "Status", render: (c) => <Badge value={c.status} /> },
            ]}
            actions={(c) => (
              <>
                <button
                  className="icon-btn"
                  aria-label="Visualizar contrato"
                  onClick={() => setDetail(c)}
                >
                  <Eye size={17} />
                </button>
                <button
                  className="icon-btn"
                  aria-label="Renovar contrato"
                  title={
                    !c.id ? "Identificador indisponível" : "Renovar contrato"
                  }
                  disabled={!c.id || c.status !== "ATIVO"}
                  onClick={() => setRenew(c)}
                >
                  <RefreshCw size={16} />
                </button>
              </>
            )}
          />
        </section>
      </State>
      {detail && (
        <Modal title="Detalhes do contrato" onClose={() => setDetail(null)}>
          <Detail
            data={{
              Sala: detail.salaNome,
              Inquilino: detail.inquilinoNome,
              Proprietário: detail.proprietarioNome,
              Início: date(detail.dataInicio),
              Fim: date(detail.dataFim),
              "Data do aviso": date(detail.dataAviso),
              "Antecedência (dias)": detail.avisoAntecedenciaDias,
              Status: detail.status,
            }}
          />
        </Modal>
      )}
      {renew && (
        <Modal title="Renovar contrato" onClose={() => setRenew(null)}>
          <p>
            Período atual até {date(renew.dataFim)} → Novo período. O contrato
            anterior será preservado como RENOVADO.
          </p>
          <Form
            fields={[
              {
                name: "dataFim",
                label: "Novo fim do contrato",
                type: "date",
                validate: (v) =>
                  v > renew.dataFim ||
                  "Informe uma data posterior ao fim atual.",
              },
              {
                name: "avisoAntecedenciaDias",
                label: "Antecedência em dias",
                type: "number",
                min: 1,
              },
            ]}
            defaults={{ avisoAntecedenciaDias: 30 }}
            onClose={() => setRenew(null)}
            onSubmit={async (d) => {
              await save.mutateAsync(d);
              setRenew(null);
            }}
          />
        </Modal>
      )}
    </>
  );
}
