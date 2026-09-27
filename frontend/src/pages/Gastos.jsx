import { useState } from "react";
import { Plus, Pencil, Trash2, Receipt } from "lucide-react";
import {
  PageHeader,
  State,
  Modal,
  Form,
  Confirm,
  SearchBox,
  RecordList,
} from "../components/UI";
import GalleryPicker from "../components/GalleryPicker";
import { useData, useAction } from "../hooks/data";
import { gastosExtrasService } from "../services/gastosExtrasService";
import { money, date, today } from "../utils/format";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
export default function Gastos() {
  const [gallery, setGallery] = useState("");
  const [search, setSearch] = useState("");
  const [start, setStart] = useState("");
  const [end, setEnd] = useState("");
  const [edit, setEdit] = useState(null);
  const [del, setDel] = useState(null);
  const valid = !start || !end || start <= end;
  const q = useData(
    ["gastos", gallery, start, end],
    () =>
      start && end
        ? gastosExtrasService.period(gallery, start, end)
        : gastosExtrasService.list(gallery),
    !!gallery && valid,
  );
  const save = useAction(
    (d) =>
      edit.id
        ? gastosExtrasService.update(edit.id, d)
        : gastosExtrasService.create(gallery, d),
    ["gastos"],
  );
  const remove = useAction((id) => gastosExtrasService.remove(id), ["gastos"]);
  const items =
    q.data?.filter((g) =>
      g.nome.toLowerCase().includes(search.toLowerCase()),
    ) || [];
  const groups = Object.entries(
    items.reduce((a, g) => {
      const k = g.dataGasto.slice(0, 7);
      a[k] = (a[k] || 0) + Number(g.valor);
      return a;
    }, {}),
  )
    .sort()
    .map(([name, valor]) => ({ name, valor }));
  return (
    <>
      <PageHeader
        title="Cuide de cada investimento."
        description="Organize as despesas que mantêm suas galerias em movimento."
        action={
          <button
            className="btn"
            disabled={!gallery}
            onClick={() => setEdit({ dataGasto: today() })}
          >
            <Plus size={18} /> Novo gasto
          </button>
        }
      />
      <GalleryPicker value={gallery} onChange={setGallery} />
      <div className="toolbar">
        <SearchBox
          value={search}
          onChange={setSearch}
          placeholder="Buscar gasto pelo nome"
        />
        <label className="field">
          <span>De</span>
          <input
            type="date"
            value={start}
            onChange={(e) => setStart(e.target.value)}
          />
        </label>
        <label className="field">
          <span>Até</span>
          <input
            type="date"
            value={end}
            onChange={(e) => setEnd(e.target.value)}
          />
        </label>
      </div>
      {!valid && (
        <p className="field-error">
          A data final deve ser igual ou posterior à inicial.
        </p>
      )}
      {!gallery ? (
        <div className="state">
          <Receipt />
          <h3>Selecione uma galeria para consultar os gastos.</h3>
        </div>
      ) : (
        valid && (
          <State query={q}>
            <div className="panel expenditure">
              <div>
                <span className="eyebrow">TOTAL NO RECORTE</span>
                <h2>{money(items.reduce((s, g) => s + Number(g.valor), 0))}</h2>
                <p>{items.length} despesas registradas</p>
              </div>
              {groups.length > 0 && (
                <div className="small-chart">
                  <ResponsiveContainer width="100%" height={160}>
                    <BarChart data={groups}>
                      <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                      <YAxis hide />
                      <Tooltip formatter={money} />
                      <Bar
                        isAnimationActive={false}
                        dataKey="valor"
                        fill="#68947A"
                        radius={[5, 5, 0, 0]}
                      />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              )}
            </div>
            <section className="panel table-panel">
              <RecordList
                items={items}
                columns={[
                  { label: "Gasto", key: "nome" },
                  { label: "Descrição", key: "descricao" },
                  { label: "Valor", render: (g) => money(g.valor) },
                  { label: "Data", render: (g) => date(g.dataGasto) },
                ]}
                actions={(g) => (
                  <>
                    <button
                      className="icon-btn"
                      aria-label={"Editar " + g.nome}
                      onClick={() => setEdit(g)}
                    >
                      <Pencil size={16} />
                    </button>
                    <button
                      className="icon-btn"
                      aria-label={"Excluir " + g.nome}
                      onClick={() => setDel(g)}
                    >
                      <Trash2 size={16} />
                    </button>
                  </>
                )}
              />
            </section>
          </State>
        )
      )}
      {edit && (
        <Modal
          title={edit.id ? "Editar gasto" : "Novo gasto"}
          onClose={() => setEdit(null)}
        >
          <Form
            fields={[
              { name: "nome", label: "Nome", minLength: 3, maxLength: 255 },
              {
                name: "descricao",
                label: "Descrição",
                minLength: 3,
                maxLength: 255,
              },
              {
                name: "valor",
                label: "Valor (R$)",
                type: "number",
                min: 0.01,
                step: 0.01,
              },
              { name: "dataGasto", label: "Data do gasto", type: "date" },
            ]}
            defaults={edit}
            onClose={() => setEdit(null)}
            onSubmit={async (d) => {
              const { nome, descricao, valor, dataGasto } = d;
              await save.mutateAsync({ nome, descricao, valor, dataGasto });
              setEdit(null);
            }}
          />
        </Modal>
      )}
      {del && (
        <Confirm
          text={`Excluir o gasto “${del.nome}” de ${money(del.valor)}?`}
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
