import { useState } from "react";
import { Eye, Pencil } from "lucide-react";
import {
  PageHeader,
  State,
  Modal,
  Form,
  SearchBox,
  RecordList,
  Detail,
  Badge,
} from "../components/UI";
import { useData, useAction } from "../hooks/data";
import { tenantsService } from "../services/tenantsService";
import { alugueisService } from "../services/alugueisService";
import { tenantFields } from "./Alugueis";
import { date } from "../utils/format";
function History({ tenant }) {
  const q = useData(["alugueis", "tenant", tenant.id], () =>
    alugueisService.tenant(tenant.id),
  );
  return (
    <>
      <Detail
        data={{
          Nome: tenant.nome,
          Documento: tenant.documentNumber,
          Tipo: tenant.documentType,
          Telefone: tenant.phone,
          Email: tenant.email,
        }}
      />
      <h3>Histórico de aluguéis</h3>
      <State query={q}>
        <RecordList
          items={q.data || []}
          columns={[
            { label: "Sala", key: "salaNome" },
            { label: "Início", render: (r) => date(r.dataInicio) },
            { label: "Status", render: (r) => <Badge value={r.status} /> },
          ]}
        />
      </State>
    </>
  );
}
export default function Inquilinos() {
  const q = useData("tenants", tenantsService.list);
  const [search, setSearch] = useState("");
  const [edit, setEdit] = useState(null);
  const [detail, setDetail] = useState(null);
  const save = useAction(
    (d) => tenantsService.update(edit.documentNumber, d),
    ["tenants", "alugueis"],
  );
  return (
    <>
      <PageHeader
        title="Pessoas que fazem parte."
        description="Consulte os inquilinos e mantenha seus contatos atualizados."
      />
      <div className="notice">
        Novos inquilinos são cadastrados ao criar um aluguel. Esta lista reúne
        os inquilinos ativos disponíveis para sua conta.
      </div>
      <div className="toolbar">
        <SearchBox
          value={search}
          onChange={setSearch}
          placeholder="Buscar nome, documento ou e-mail"
        />
      </div>
      <State query={q}>
        <section className="panel table-panel">
          <RecordList
            items={
              q.data?.filter((t) =>
                `${t.nome} ${t.documentNumber} ${t.email}`
                  .toLowerCase()
                  .includes(search.toLowerCase()),
              ) || []
            }
            columns={[
              { label: "Nome", key: "nome" },
              { label: "Documento", key: "documentNumber" },
              { label: "E-mail", key: "email" },
              { label: "Telefone", key: "phone" },
            ]}
            actions={(t) => (
              <>
                <button
                  className="icon-btn"
                  aria-label={"Ver " + t.nome}
                  onClick={() => setDetail(t)}
                >
                  <Eye size={17} />
                </button>
                <button
                  className="icon-btn"
                  aria-label={"Editar " + t.nome}
                  onClick={() => setEdit(t)}
                >
                  <Pencil size={17} />
                </button>
              </>
            )}
          />
        </section>
      </State>
      {edit && (
        <Modal title="Editar inquilino" onClose={() => setEdit(null)}>
          <Form
            fields={tenantFields.map((f) =>
              ["documentType", "documentNumber"].includes(f.name)
                ? { ...f, disabled: true, required: false }
                : f,
            )}
            defaults={{ ...edit, name: edit.nome }}
            onClose={() => setEdit(null)}
            onSubmit={async (d) => {
              const { name, phone, email, documentType, documentNumber } = d;
              await save.mutateAsync({
                name,
                phone,
                email,
                documentType: edit.documentType,
                documentNumber: edit.documentNumber,
              });
              setEdit(null);
            }}
          />
        </Modal>
      )}
      {detail && (
        <Modal title="Inquilino" onClose={() => setDetail(null)}>
          <History tenant={detail} />
        </Modal>
      )}
    </>
  );
}
