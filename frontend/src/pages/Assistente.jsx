import { useState } from "react";
import { Send, Sparkles, UserRound, MessageSquare } from "lucide-react";
import { PageHeader } from "../components/UI";
import { initialConversations, localReply } from "../mocks/chat";
export default function Assistente() {
  const [conversations, setConversations] = useState(() =>
    structuredClone(initialConversations),
  );
  const [selected, setSelected] = useState(conversations[0].id);
  const [text, setText] = useState("");
  const [human, setHuman] = useState(false);
  const conversation = conversations.find((c) => c.id === selected);
  return (
    <>
      <PageHeader
        title="Conversas que aproximam."
        description="Um espaço para acompanhar o atendimento dos seus inquilinos."
      />
      <div className="notice">
        <Sparkles size={17} /> Prévia interativa · As mensagens ficam apenas
        nesta tela. Não há envio real nem IA conectada.
      </div>
      <div className="chat-layout">
        <aside className="chat-sidebar">
          <h3>
            Conversas <span>02</span>
          </h3>
          {conversations.map((c) => (
            <button
              key={c.id}
              className={selected === c.id ? "selected" : ""}
              onClick={() => setSelected(c.id)}
            >
              <span className="avatar">
                <MessageSquare size={19} />
              </span>
              <span>
                <strong>{c.name}</strong>
                <small>{c.subtitle}</small>
              </span>
            </button>
          ))}
        </aside>
        <section className="chat-main">
          <header>
            <div>
              <strong>{conversation.name}</strong>
              <small>Demonstração</small>
            </div>
            <button
              className={"btn " + (human ? "" : "secondary")}
              onClick={() => setHuman(!human)}
            >
              <UserRound size={16} />
              {human ? "Intervenção ativa" : "Intervir"}
            </button>
          </header>
          <div className="messages" aria-live="polite">
            {conversation.messages.map((m) => (
              <div key={m.id} className={"message " + m.author}>
                <small>
                  {m.author === "ia"
                    ? "Assistente · exemplo"
                    : "Você · mensagem local"}
                </small>
                <p>{m.text}</p>
              </div>
            ))}
          </div>
          <form
            className="chat-input"
            onSubmit={(e) => {
              e.preventDefault();
              if (!text.trim()) return;
              setConversations((cs) =>
                cs.map((c) =>
                  c.id === selected
                    ? {
                        ...c,
                        messages: [
                          ...c.messages,
                          {
                            id: crypto.randomUUID(),
                            author: "user",
                            text: text.trim(),
                          },
                          {
                            id: crypto.randomUUID(),
                            author: "ia",
                            text: localReply,
                          },
                        ],
                      }
                    : c,
                ),
              );
              setText("");
            }}
          >
            <input
              aria-label="Mensagem"
              placeholder="Escreva uma mensagem de teste…"
              value={text}
              onChange={(e) => setText(e.target.value)}
            />
            <button
              className="btn"
              aria-label="Enviar mensagem de teste"
              disabled={!text.trim()}
            >
              <Send size={19} />
            </button>
          </form>
        </section>
      </div>
    </>
  );
}
