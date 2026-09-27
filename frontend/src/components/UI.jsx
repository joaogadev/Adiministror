import { useEffect, useRef, useState } from "react";
import { useForm } from "react-hook-form";
import {
  X,
  Inbox,
  AlertCircle,
  Search,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";
import { message } from "../services/api";
export function PageHeader({
  eyebrow = "SEU ESPAÇO DE GESTÃO",
  title,
  description,
  action,
  level = 1,
}) {
  const Heading = level === 1 ? "h1" : "h2";
  return (
    <div className="page-heading">
      <div>
        <div className="eyebrow">{eyebrow}</div>
        <Heading>{title}</Heading>
        <p>{description}</p>
      </div>
      {action}
    </div>
  );
}
export function Badge({ value }) {
  return (
    <span className={"badge " + String(value).toLowerCase()}>
      {String(value).replaceAll("_", " ")}
    </span>
  );
}
export function State({
  query,
  children,
  empty = "Nenhum registro encontrado.",
  action,
  allowEmpty = false,
}) {
  if (query.isPending)
    return (
      <div className="skeleton-grid" aria-label="Carregando" role="status">
        {[1, 2, 3].map((n) => (
          <div className="skeleton" key={n} />
        ))}
      </div>
    );
  if (query.isError)
    return (
      <div className="state error" role="alert">
        <AlertCircle />
        <h3>Não foi possível carregar</h3>
        <p>{message(query.error)}</p>
        <button className="btn secondary" onClick={() => query.refetch()}>
          Tentar novamente
        </button>
      </div>
    );
  if (!query.data || (!allowEmpty && Array.isArray(query.data) && !query.data.length))
    return (
      <div className="state">
        <Inbox />
        <h3>{empty}</h3>
        <p>Seus registros aparecerão aqui assim que estiverem disponíveis.</p>
        {action}
      </div>
    );
  return children;
}
export function Modal({ title, onClose, children }) {
  const ref = useRef();
  useEffect(() => {
    const previous = document.activeElement;
    const dialog = ref.current;
    dialog.showModal();
    return () => {
      dialog.close();
      previous?.focus();
    };
  }, []);
  return (
    <dialog
      aria-label={title}
      ref={ref}
      onCancel={onClose}
      onClick={(e) => {
        if (e.target === ref.current) onClose();
      }}
    >
      <div className="modal-head">
        <h2>{title}</h2>
        <button className="icon-btn" aria-label="Fechar" onClick={onClose}>
          <X size={20} />
        </button>
      </div>
      <div className="modal-body">{children}</div>
    </dialog>
  );
}
export function Confirm({
  title = "Confirmar exclusão",
  text,
  onClose,
  onConfirm,
  pending,
}) {
  return (
    <Modal title={title} onClose={onClose}>
      <div className="confirm-icon">
        <AlertCircle />
      </div>
      <p>{text}</p>
      <div className="form-actions">
        <button className="btn secondary" onClick={onClose} disabled={pending}>
          Cancelar
        </button>
        <button className="btn danger" disabled={pending} onClick={onConfirm}>
          {pending ? "Aguarde…" : "Confirmar"}
        </button>
      </div>
    </Modal>
  );
}
const read = (obj, path) => path.split(".").reduce((o, k) => o?.[k], obj);
export function Field({ field, form }) {
  const {
    name,
    label,
    type = "text",
    options,
    required = true,
    ...rest
  } = field;
  const error = read(form.formState.errors, name);
  const rules = {
    required: required ? "Preencha este campo." : false,
    ...(rest.min !== undefined
      ? { min: { value: rest.min, message: `Mínimo: ${rest.min}` } }
      : {}),
    ...(rest.max !== undefined
      ? { max: { value: rest.max, message: `Máximo: ${rest.max}` } }
      : {}),
    ...(rest.minLength
      ? {
          minLength: {
            value: rest.minLength,
            message: `Use pelo menos ${rest.minLength} caracteres.`,
          },
        }
      : {}),
    ...(rest.pattern
      ? {
          pattern: {
            value: rest.pattern,
            message: rest.patternMessage || "Formato inválido.",
          },
        }
      : {}),
    ...(rest.validate ? { validate: rest.validate } : {}),
  };
  const { pattern, patternMessage, validate, ...attrs } = rest;
  return (
    <label className="field" htmlFor={name}>
      <span>
        {label}
        {!required && <small> opcional</small>}
      </span>
      {options ? (
        <select
          id={name}
          aria-invalid={!!error}
          {...form.register(name, rules)}
          {...attrs}
        >
          <option value="">Selecione</option>
          {options.map((o) => (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
      ) : type === "textarea" ? (
        <textarea
          id={name}
          aria-invalid={!!error}
          {...form.register(name, rules)}
          {...attrs}
        />
      ) : (
        <input
          id={name}
          type={type}
          aria-invalid={!!error}
          {...form.register(name, {
            ...rules,
            ...(type === "number" ? { valueAsNumber: true } : {}),
          })}
          {...attrs}
        />
      )}{" "}
      {error && (
        <small className="field-error" role="alert">
          {error.message}
        </small>
      )}
    </label>
  );
}
export function Form({
  fields,
  defaults = {},
  onSubmit,
  onClose,
  submit = "Salvar",
  children,
}) {
  const form = useForm({ defaultValues: defaults });
  const [general, setGeneral] = useState("");
  const submitForm = async (data) => {
    setGeneral("");
    try {
      await onSubmit(data);
    } catch (e) {
      const errors = e.response?.data?.validationErrors || {};
      Object.entries(errors).forEach(([key, value]) =>
        form.setError(key, { message: value }),
      );
      setGeneral(message(e));
    }
  };
  return (
    <form onSubmit={form.handleSubmit(submitForm)}>
      <div className="form-grid">
        {fields.map((field) => (
          <Field key={field.name} field={field} form={form} />
        ))}
      </div>
      {children}
      {general && (
        <p className="field-error" role="alert">
          {general}
        </p>
      )}
      <div className="form-actions">
        {onClose && (
          <button type="button" className="btn secondary" onClick={onClose}>
            Cancelar
          </button>
        )}
        <button className="btn" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Salvando…" : submit}
        </button>
      </div>
    </form>
  );
}
export function SearchBox({ value, onChange, placeholder = "Buscar…" }) {
  return (
    <label className="search">
      <Search size={18} />
      <input
        aria-label={placeholder}
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
    </label>
  );
}
export function RecordList({ items, columns, actions }) {
  const [page, setPage] = useState(1);
  useEffect(() => setPage(1), [items.length]);
  const pages = Math.max(1, Math.ceil(items.length / 8));
  const current = Math.min(page, pages);
  return (
    <>
      <div className="record-list">
        <div
          className="record-header"
          style={{
            gridTemplateColumns: `repeat(${columns.length},minmax(0,1fr)) ${actions ? "minmax(100px, .7fr)" : ""}`,
          }}
        >
          {columns.map((c) => (
            <span key={c.label}>{c.label}</span>
          ))}
          {actions && <span>Ações</span>}
        </div>
        {items.slice((current - 1) * 8, current * 8).map((item, i) => (
          <div
            className="record-row"
            key={item.id || i}
            style={{
              gridTemplateColumns: `repeat(${columns.length},minmax(0,1fr)) ${actions ? "minmax(100px, .7fr)" : ""}`,
            }}
          >
            {columns.map((c) => (
              <div className="record-cell" key={c.label}>
                <span className="mobile-label">{c.label}</span>
                {c.render ? c.render(item) : item[c.key] || "—"}
              </div>
            ))}
            {actions && <div className="row-actions">{actions(item)}</div>}
          </div>
        ))}
      </div>
      {!items.length && (
        <div className="state compact">
          <Inbox />
          <p>Nenhum resultado para os filtros selecionados.</p>
        </div>
      )}
      <div className="pagination">
        <span>{items.length} registros</span>
        <div>
          <button
            className="icon-btn"
            aria-label="Página anterior"
            disabled={current <= 1}
            onClick={() => setPage(current - 1)}
          >
            <ChevronLeft size={18} />
          </button>
          <span>
            {current} de {pages}
          </span>
          <button
            className="icon-btn"
            aria-label="Próxima página"
            disabled={current >= pages}
            onClick={() => setPage(current + 1)}
          >
            <ChevronRight size={18} />
          </button>
        </div>
      </div>
    </>
  );
}
export function Detail({ data }) {
  return (
    <dl className="details">
      {Object.entries(data).map(([k, v]) => (
        <div key={k}>
          <dt>{k}</dt>
          <dd>{v ?? "—"}</dd>
        </div>
      ))}
    </dl>
  );
}
