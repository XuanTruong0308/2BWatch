import React, { useEffect, useMemo, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client/dist/sockjs";
import { MessageCircle, Send, User } from "lucide-react";
import { getJson } from "@/lib/api/client";
import { useI18n } from "@/lib/i18n";

type ChatMessage = {
  senderId: string;
  senderName: string;
  content: string;
  isAdmin: boolean;
  timestamp?: string;
};

type SupportChatSession = {
  sessionId: string;
  customerName: string;
  messages: ChatMessage[];
  lastMessage: ChatMessage | null;
};

type SupportChatSessionEvent = {
  type: string;
  sessionId: string;
  reason: string;
};

export default function AdminSupportChatPage() {
  const { tx } = useI18n();
  const [sessions, setSessions] = useState<Record<string, SupportChatSession>>({});
  const [activeSessionId, setActiveSessionId] = useState<string | null>(null);
  const [input, setInput] = useState("");
  const stompClientRef = useRef<Client | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    let active = true;

    getJson<{ data: SupportChatSession[] }>("/api/v1/admin/support-chat/sessions")
      .then((response) => {
        if (!active) {
          return;
        }

        const nextSessions = response.data.reduce<Record<string, SupportChatSession>>((accumulator, session) => {
          accumulator[session.sessionId] = session;
          return accumulator;
        }, {});

        setSessions(nextSessions);

        if (!activeSessionId && response.data.length > 0) {
          setActiveSessionId(response.data[0].sessionId);
        }
      })
      .catch((error) => {
        console.error("Failed to load support chat sessions", error);
      });

    return () => {
      active = false;
    };
  }, [activeSessionId]);

  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [sessions, activeSessionId]);

  useEffect(() => {
    if (activeSessionId && sessions[activeSessionId]) {
      return;
    }

    const nextSessionId = Object.keys(sessions)[0] ?? null;
    if (nextSessionId !== activeSessionId) {
      setActiveSessionId(nextSessionId);
    }
  }, [activeSessionId, sessions]);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS("/ws"),
      reconnectDelay: 5000,
    });

    client.onConnect = () => {
      client.subscribe("/topic/admin", (message) => {
        const newMessage: ChatMessage = JSON.parse(message.body);

        setSessions((prev) => {
          const currentSession = prev[newMessage.senderId] ?? {
            sessionId: newMessage.senderId,
            customerName: newMessage.senderName || "Customer",
            messages: [],
            lastMessage: null,
          };

          const updatedSession: SupportChatSession = {
            ...currentSession,
            customerName: currentSession.customerName || newMessage.senderName || "Customer",
            messages: [...currentSession.messages, newMessage],
            lastMessage: newMessage,
          };

          return {
            ...prev,
            [newMessage.senderId]: updatedSession,
          };
        });

        setActiveSessionId((current) => current ?? newMessage.senderId);
      });

      client.subscribe("/topic/admin.chat.sessions", (message) => {
        const event: SupportChatSessionEvent = JSON.parse(message.body);
        if (event.type !== "SESSION_CLEARED") {
          return;
        }

        setSessions((prev) => {
          if (!prev[event.sessionId]) {
            return prev;
          }

          const nextSessions = { ...prev };
          delete nextSessions[event.sessionId];
          return nextSessions;
        });
      });
    };

    client.activate();
    stompClientRef.current = client;

    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
    };
  }, []);

  const sendMessage = (event: React.FormEvent) => {
    event.preventDefault();
    if (!input.trim() || !activeSessionId || !stompClientRef.current?.active) {
      return;
    }

    const message: ChatMessage = {
      senderId: activeSessionId,
      senderName: tx("Hỗ trợ vien", "Admin support"),
      content: input.trim(),
      isAdmin: true,
    };

    stompClientRef.current.publish({
      destination: "/app/chat",
      body: JSON.stringify(message),
    });

    setInput("");
  };

  const orderedSessions = useMemo(
    () =>
      Object.values(sessions).sort((left, right) => {
        const leftTime = left.lastMessage?.timestamp ? new Date(left.lastMessage.timestamp).getTime() : 0;
        const rightTime = right.lastMessage?.timestamp ? new Date(right.lastMessage.timestamp).getTime() : 0;
        return rightTime - leftTime;
      }),
    [sessions],
  );

  const activeSession = activeSessionId ? sessions[activeSessionId] ?? null : null;
  const activeMessages = activeSession?.messages ?? [];

  return (
    <div className="stack-section">
      <div className="section-heading">
        <div>
          <span className="eyebrow">{tx("Hỗ trợ trực tuyến", "Live support")}</span>
          <h2>{tx("Quản lý hội thoại với khách hàng", "Manage customer conversations")}</h2>
          <p className="muted-copy">
            {tx(
              "Hội thoại theo session sẽ tự động biến mất khi phiên đăng nhập liên quan hết hạn.",
              "Session-based threads disappear automatically when the related login session expires.",
            )}
          </p>
        </div>
      </div>

      <div className="chat-admin">
        <aside className="chat-sidebar">
          <div className="chat-sidebar__header">
            <span className="eyebrow">{tx("Phiên đang hoạt động", "Active sessions")}</span>
            <h3 style={{ marginTop: 8 }}>{tx("Hộp thư hỗ trợ", "Support inbox")}</h3>
          </div>

          <div className="chat-sidebar__list">
            {orderedSessions.length === 0 ? (
              <div className="chat-thread__empty">{tx("Chưa có hội thoại khách hàng nào đang hoạt động.", "No active customer conversation yet.")}</div>
            ) : (
              orderedSessions.map((session) => (
                <button
                  key={session.sessionId}
                  type="button"
                  className={`chat-session-item ${activeSessionId === session.sessionId ? "is-active" : ""}`}
                  onClick={() => setActiveSessionId(session.sessionId)}
                >
                  <div className="chat-session-item__avatar">
                    <User size={18} />
                  </div>
                  <div className="chat-session-item__meta">
                    <p>{session.customerName || tx("Khách hàng", "Customer")}</p>
                    <span>{session.lastMessage?.content ?? tx("Chưa có tin nhắn", "No messages yet")}</span>
                  </div>
                </button>
              ))
            )}
          </div>
        </aside>

        <section className="chat-thread">
          {!activeSession ? (
            <div className="chat-thread__empty">
              <div>
                <MessageCircle size={26} style={{ margin: "0 auto 12px" }} />
                <p>{tx("Chon mot cuoc hoi thoai ben trai de bat dau tra loi.", "Select a conversation from the left to start replying.")}</p>
              </div>
            </div>
          ) : (
            <>
              <div className="chat-thread__header">
                <MessageCircle size={18} />
                <div>
                  <h4>{activeSession.customerName || tx("Khách hàng", "Customer")}</h4>
                  <p>
                    {tx("Phiên", "Session")} {activeSession.sessionId.slice(0, 8)}
                  </p>
                </div>
              </div>

              <div className="chat-thread__body">
                {activeMessages.map((message, index) => {
                  const isMine = message.isAdmin === true;

                  return (
                    <div
                      key={`${message.timestamp ?? "message"}-${index}`}
                      className={`chat-message ${isMine ? "chat-message--mine" : ""}`}
                    >
                      {!isMine ? <span className="chat-message__sender">{message.senderName}</span> : null}
                      <div className="chat-message__bubble">{message.content}</div>
                    </div>
                  );
                })}

                <div ref={messagesEndRef} />
              </div>

              <div className="chat-thread__footer">
                <form className="chat-form" onSubmit={sendMessage}>
                  <input
                    className="field"
                    type="text"
                    value={input}
                    onChange={(event) => setInput(event.target.value)}
                    placeholder={tx(
                      `Trả lời ${activeSession.customerName || "khách hàng"}...`,
                      `Reply to ${activeSession.customerName || "customer"}...`,
                    )}
                  />
                  <button className="button button-primary" type="submit" disabled={!input.trim()}>
                    <Send size={18} />
                  </button>
                </form>
              </div>
            </>
          )}
        </section>
      </div>
    </div>
  );
}
