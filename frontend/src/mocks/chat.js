// TODO BACKEND: conversas, envio de mensagens e intervenção humana. Apenas demonstração local.
export const initialConversations = [
  {
    id: "example-1",
    name: "Conversa de exemplo",
    subtitle: "Conheça o futuro assistente",
    messages: [
      {
        id: "welcome",
        author: "ia",
        text: "Este é um espaço de demonstração. No futuro, você poderá acompanhar conversas e intervir quando necessário. Nenhuma mensagem é enviada a um inquilino.",
      },
    ],
  },
  {
    id: "example-2",
    name: "Atendimento de exemplo",
    subtitle: "Organize suas conversas",
    messages: [
      {
        id: "intro",
        author: "ia",
        text: "Aqui ficará o histórico do atendimento. A integração com IA ainda não está disponível.",
      },
    ],
  },
];
export const localReply =
  "Mensagem adicionada à demonstração local. Ainda não há conexão com uma IA ou com inquilinos.";
