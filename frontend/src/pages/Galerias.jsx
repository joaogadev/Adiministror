import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import {
  Building2,
  MapPin,
  ArrowUpRight,
  Plus,
  Pencil,
  Trash2,
  Phone,
} from "lucide-react";
import {
  PageHeader,
  State,
  Modal,
  Form,
  Confirm,
  SearchBox,
  Detail,
} from "../components/UI";
import { useData, useAction } from "../hooks/data";
import { galeriasService } from "../services/galeriasService";
import { useAuth } from "../contexts/AuthContext";
import Salas from "./Salas";
const fields = [
  { name: "nome", label: "Nome da galeria", maxLength: 100 },
  {
    name: "phone",
    label: "Telefone",
    pattern: /^\+?[0-9]{10,15}$/,
    placeholder: "79999999999",
  },
  {
    name: "endereco.zipCode",
    label: "CEP (somente números)",
    pattern: /^[0-9]{8}$/,
  },
  { name: "endereco.estado", label: "Estado (UF)", minLength: 2, maxLength: 2 },
  { name: "endereco.cidade", label: "Cidade", maxLength: 255 },
  { name: "endereco.bairro", label: "Bairro", maxLength: 255 },
  { name: "endereco.rua", label: "Rua", maxLength: 255 },
  { name: "endereco.numero", label: "Número", maxLength: 255 },
  {
    name: "endereco.complemento",
    label: "Complemento",
    required: false,
    maxLength: 255,
  },
];
function GalleryCard({ g, onEdit, onDelete }) {
  const count = useData(["salas", "count", g.id], () =>
    galeriasService.count(g.id),
  );
  return (
    <article className="gallery-card">
      <div className="gallery-visual">
        <span className="gallery-tag">GALERIA COMERCIAL</span>
        <div className="facade" aria-hidden="true">
          <div />
          <div />
          <div />
          <div />
        </div>
        <span className="gallery-location">
          <MapPin size={13} />
          {g.endereco?.cidade} · {g.endereco?.estado}
        </span>
      </div>
      <div className="gallery-content">
        <div className="flex-between">
          <h3>{g.nome}</h3>
          <Building2 size={19} />
        </div>
        <p>
          {g.endereco?.rua}, {g.endereco?.numero}
        </p>
        <div className="gallery-meta">
          <span>
            {count.isSuccess
              ? `${count.data} salas`
              : count.isError
                ? "Salas indisponíveis"
                : "Carregando salas…"}
          </span>
          <span>
            <Phone size={13} />
            {g.phone || "Não informado"}
          </span>
        </div>
        <div className="gallery-actions">
          <Link to={"/galerias/" + g.id}>
            Ver galeria <ArrowUpRight size={17} />
          </Link>
          <div>
            <button
              className="icon-btn"
              aria-label={"Editar " + g.nome}
              onClick={() => onEdit(g)}
            >
              <Pencil size={16} />
            </button>
            <button
              className="icon-btn"
              aria-label={"Excluir " + g.nome}
              onClick={() => onDelete(g)}
            >
              <Trash2 size={16} />
            </button>
          </div>
        </div>
      </div>
    </article>
  );
}
export default function Galerias() {
  const { isAdmin } = useAuth();
  const [search, setSearch] = useState("");
  const [term, setTerm] = useState("");
  const [edit, setEdit] = useState(null);
  const [del, setDel] = useState(null);
  const q = useData(["galerias", "list", term], () =>
    term ? galeriasService.search(term) : galeriasService.list(),
  );
  const save = useAction(
    (d) =>
      edit.id ? galeriasService.update(edit.id, d) : galeriasService.create(d),
    ["galerias"],
  );
  const remove = useAction(
    (id) => galeriasService.remove(id),
    ["galerias", "salas", "gastos"],
  );
  return (
    <>
      <PageHeader
        title="Seus espaços, novas possibilidades."
        description="Organize suas galerias e acompanhe cada espaço de perto."
        action={
          <button className="btn" onClick={() => setEdit({})}>
            <Plus size={18} /> Nova galeria
          </button>
        }
      />
      {isAdmin && (
        <div className="notice">
          A lista inicial mostra suas galerias. Use a busca por nome para
          localizar galerias de outros proprietários.
        </div>
      )}
      <form
        className="toolbar"
        onSubmit={(e) => {
          e.preventDefault();
          setTerm(search.trim());
        }}
      >
        <SearchBox
          value={search}
          onChange={setSearch}
          placeholder="Buscar galerias pelo nome"
        />
        <button className="btn secondary">Buscar</button>
        <span className="muted">{q.data?.length || 0} galerias</span>
      </form>
      <State
        query={q}
        empty="Você ainda não cadastrou nenhuma galeria."
        action={
          <button className="btn" onClick={() => setEdit({})}>
            Criar minha primeira galeria
          </button>
        }
      >
        <div className="gallery-grid">
          {q.data?.map((g) => (
            <GalleryCard key={g.id} g={g} onEdit={setEdit} onDelete={setDel} />
          ))}
        </div>
      </State>
      {edit && (
        <Modal
          title={edit.id ? "Editar galeria" : "Uma nova galeria"}
          onClose={() => setEdit(null)}
        >
          <Form
            fields={edit.id ? fields.slice(0, 2) : fields}
            defaults={edit}
            onClose={() => setEdit(null)}
            onSubmit={async (d) => {
              await save.mutateAsync({
                ...d,
                endereco: edit.id
                  ? edit.endereco
                  : { ...d.endereco, estado: d.endereco.estado.toUpperCase() },
              });
              setEdit(null);
            }}
          >
            {edit.id && (
              <div className="notice">
                O endereço atual é preservado. A alteração de endereço ainda não
                está disponível.
              </div>
            )}
          </Form>
        </Modal>
      )}
      {del && (
        <Confirm
          text={`Excluir a galeria “${del.nome}”? Esta ação remove o registro e pode ser impedida se houver vínculos.`}
          onClose={() => setDel(null)}
          pending={remove.isPending}
          onConfirm={() =>
            remove.mutate(del.id, { onSuccess: () => setDel(null) })
          }
        />
      )}
    </>
  );
}
export function GaleriaDetail() {
  const { id } = useParams();
  const q = useData(["galerias", id], () => galeriasService.get(id));
  return (
    <>
      <Link className="back-link" to="/galerias">
        ← Voltar para galerias
      </Link>
      <State query={q}>
        {q.data && (
          <>
            <PageHeader
              title={q.data.nome}
              description="Informações e salas desta galeria."
            />
            <section className="panel">
              <Detail
                data={{
                  Endereço: `${q.data.endereco.rua}, ${q.data.endereco.numero}`,
                  Cidade: `${q.data.endereco.cidade} / ${q.data.endereco.estado}`,
                  Bairro: q.data.endereco.bairro,
                  CEP: q.data.endereco.zipCode,
                  Telefone: q.data.phone,
                  Proprietário: q.data.dono?.nome,
                }}
              />
            </section>
            <Salas galleryId={id} />
          </>
        )}
      </State>
    </>
  );
}
