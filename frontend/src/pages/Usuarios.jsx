import { useState } from "react";
import { Eye } from "lucide-react";
import {
  PageHeader,
  State,
  SearchBox,
  RecordList,
  Badge,
  Modal,
  Detail,
} from "../components/UI";
import { useData } from "../hooks/data";
import { usuariosService } from "../services/usuariosService";
export default function Usuarios() {
  const q = useData("usuarios", usuariosService.list);
  const [search, setSearch] = useState("");
  const [role, setRole] = useState("");
  const [detail, setDetail] = useState(null);
  return (
    <>
      <PageHeader
        eyebrow="ADMINISTRAÇÃO"
        title="Pessoas por trás dos negócios."
        description="Consulte as contas cadastradas e seus perfis de acesso."
      />
      <div className="toolbar">
        <SearchBox
          value={search}
          onChange={setSearch}
          placeholder="Buscar nome ou e-mail"
        />
        <select
          aria-label="Perfil do usuário"
          value={role}
          onChange={(e) => setRole(e.target.value)}
        >
          <option value="">Todos os perfis</option>
          <option>DONO</option>
          <option>ADMINISTRADOR</option>
        </select>
      </div>
      <State query={q}>
        <section className="panel table-panel">
          <RecordList
            items={
              q.data?.filter(
                (u) =>
                  (!role || u.role === role) &&
                  `${u.nome} ${u.email}`
                    .toLowerCase()
                    .includes(search.toLowerCase()),
              ) || []
            }
            columns={[
              { label: "Nome", key: "nome" },
              { label: "E-mail", key: "email" },
              { label: "Telefone", key: "phone" },
              { label: "Perfil", render: (u) => <Badge value={u.role} /> },
            ]}
            actions={(u) => (
              <button
                className="icon-btn"
                aria-label={"Visualizar " + u.nome}
                onClick={() => setDetail(u)}
              >
                <Eye size={17} />
              </button>
            )}
          />
        </section>
      </State>
      {detail && (
        <Modal title="Conta do usuário" onClose={() => setDetail(null)}>
          <Detail
            data={{
              Nome: detail.nome,
              Email: detail.email,
              Telefone: detail.phone,
              Perfil: detail.role,
            }}
          />
        </Modal>
      )}
    </>
  );
}
