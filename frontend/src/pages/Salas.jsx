import { useState } from "react";
import { Link } from "react-router-dom";
import { Plus, Pencil, Trash2, DoorOpen } from "lucide-react";
import {
  PageHeader,
  State,
  Modal,
  Form,
  Confirm,
  SearchBox,
  Badge,
} from "../components/UI";
import GalleryPicker from "../components/GalleryPicker";
import { useData, useAction } from "../hooks/data";
import { salasService } from "../services/salasService";
import { alugueisService } from "../services/alugueisService";
import { occupied } from "../adapters/entities";
export default function Salas({ galleryId }) {
  const [selected, setSelected] = useState("");
  const id = galleryId || selected;
  const [search, setSearch] = useState("");
  const [edit, setEdit] = useState(null);
  const [del, setDel] = useState(null);
  const q = useData(["salas", id], () => salasService.list(id), !!id);
  const rents = useData("alugueis", alugueisService.list);
  const save = useAction(
    (d) =>
      edit.id ? salasService.update(edit.id, d) : salasService.create(id, d),
    ["salas"],
  );
  const remove = useAction((id) => salasService.remove(id), ["salas"]);
  return (
    <>
      <PageHeader
        level={galleryId ? 2 : 1}
        title="Salas"
        description="Cada porta, uma oportunidade para o seu negócio."
        action={
          <button className="btn" disabled={!id} onClick={() => setEdit({})}>
            <Plus size={18} /> Nova sala
          </button>
        }
      />
      {!galleryId && <GalleryPicker value={id} onChange={setSelected} />}
      <div className="toolbar">
        <SearchBox
          value={search}
          onChange={setSearch}
          placeholder="Filtrar salas pelo nome"
        />
      </div>
      {!id ? (
        <div className="state">
          <DoorOpen />
          <h3>Escolha uma galeria para começar.</h3>
        </div>
      ) : (
        <State query={q} empty="Esta galeria ainda não possui salas.">
          <div className="gallery-grid">
            {q.data
              ?.filter((s) =>
                s.nome.toLowerCase().includes(search.toLowerCase()),
              )
              .map((s) => (
                <article className="panel room-card" key={s.id}>
                  <div className="flex-between">
                    <span className="room-icon">
                      <DoorOpen />
                    </span>
                    <Badge
                      value={
                        rents.isSuccess
                          ? occupied(s.id, rents.data)
                            ? "OCUPADA"
                            : "DISPONÍVEL"
                          : "CONSULTANDO"
                      }
                    />
                  </div>
                  <h3>{s.nome}</h3>
                  <p>
                    {rents.isError
                      ? "Não foi possível consultar a ocupação."
                      : rents.data?.find(
                          (a) => a.salaId === s.id && a.status === "ATIVO",
                        )?.tenantNome || "Espaço da galeria"}
                  </p>
                  <div className="gallery-actions">
                    <Link to="/alugueis">Ver aluguéis</Link>
                    <div>
                      <button
                        className="icon-btn"
                        aria-label={"Editar " + s.nome}
                        onClick={() => setEdit(s)}
                      >
                        <Pencil size={16} />
                      </button>
                      <button
                        className="icon-btn"
                        aria-label={"Excluir " + s.nome}
                        onClick={() => setDel(s)}
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </div>
                </article>
              ))}
          </div>
        </State>
      )}
      {edit && (
        <Modal
          title={edit.id ? "Editar sala" : "Nova sala"}
          onClose={() => setEdit(null)}
        >
          <Form
            fields={[
              {
                name: "nome",
                label: "Nome da sala",
                minLength: 3,
                maxLength: 255,
              },
            ]}
            defaults={edit}
            onClose={() => setEdit(null)}
            onSubmit={async (d) => {
              await save.mutateAsync(d);
              setEdit(null);
            }}
          />
        </Modal>
      )}
      {del && (
        <Confirm
          text={`Excluir “${del.nome}”? Salas com vínculos podem não permitir exclusão.`}
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
