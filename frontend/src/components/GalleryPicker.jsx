import { useState } from "react";
import { useData } from "../hooks/data";
import { galeriasService } from "../services/galeriasService";
import { useAuth } from "../contexts/AuthContext";
import { State, SearchBox } from "./UI";
export default function GalleryPicker({ value, onChange }) {
  const { isAdmin } = useAuth();
  const [text, setText] = useState("");
  const [term, setTerm] = useState("");
  const q = useData(["galerias", "picker", term], () =>
    term ? galeriasService.search(term) : galeriasService.list(),
  );
  return (
    <div className="gallery-picker">
      {isAdmin && (
        <form
          className="toolbar"
          onSubmit={(e) => {
            e.preventDefault();
            setTerm(text.trim());
          }}
        >
          <SearchBox
            value={text}
            onChange={setText}
            placeholder="Buscar galeria pelo nome"
          />
          <button className="btn secondary">Buscar</button>
        </form>
      )}
      <State query={q} empty="Nenhuma galeria disponível.">
        <label className="field">
          <span>Galeria {isAdmin && !term ? "(minhas galerias)" : ""}</span>
          <select value={value} onChange={(e) => onChange(e.target.value)}>
            <option value="">Selecione uma galeria</option>
            {q.data?.map((g) => (
              <option key={g.id} value={g.id}>
                {g.nome}
              </option>
            ))}
          </select>
        </label>
      </State>
    </div>
  );
}
