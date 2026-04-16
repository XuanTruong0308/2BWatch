import { useEffect, useRef, useState, type FormEvent } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client/dist/sockjs";
import { MessageCircle, Send, X } from "lucide-react";
import { getJson } from "@/lib/api/client";
import { useI18n } from "@/lib/i18n";

type ChatMessage = {
  senderId: string;
  senderName: string;
  content: string;
  isAdmin: boolean;
  timestamp?: string;
};

type SupportChatBootstrap = {
  sessionId: string;
  messages: ChatMessage[];
};

type SupportChatSessionEvent = {
  type: string;
  sessionId: string;
  reason: string;
};

export function ChatBubble() {
  const { tx } = useI18n();
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState("");
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [isBootstrapped, setIsBootstrapped] = useState(false);
  const [isConnected, setIsConnected] = useState(false);
  const stompClientRef = useRef<Client | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const applyBootstrap = (data: SupportChatBootstrap) => {
    setSessionId(data.sessionId);
    setMessages(data.messages ?? []);
    setIsBootstrapped(true);
  };

  useEffect(() => {
    let active = true;

    getJson<{ data: SupportChatBootstrap }>("/api/v1/public/support-chat/session")
      .then((response) => {
        if (!active) {
          return;
        }

        applyBootstrap(response.data);
      })
      .catch((error) => {
        console.error("Failed to bootstrap support chat", error);
      });

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    if (!sessionId) {
      return;
    }

    const client = new Client({
      webSocketFactory: () => new SockJS("/ws"),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      setIsConnected(true);

      client.subscribe(`/topic/chat.${sessionId}`, (message) => {
        const newMessage: ChatMessage = JSON.parse(message.body);
        setMessages((prev) => [...prev, newMessage]);
      });

      client.subscribe(`/topic/chat.session.${sessionId}`, async (message) => {
        const event: SupportChatSessionEvent = JSON.parse(message.body);
        if (event.type !== "SESSION_CLEARED") {
          return;
        }

        setMessages([]);
        setInput("");
        setIsBootstrapped(false);
        setIsConnected(false);

        try {
          const response = await getJson<{ data: SupportChatBootstrap }>("/api/v1/public/support-chat/session");
          applyBootstrap(response.data);
        } catch (error) {
          console.error("Failed to refresh support chat session", error);
        }
      });
    };

    client.onWebSocketClose = () => {
      setIsConnected(false);
    };

    client.onStompError = (frame) => {
      setIsConnected(false);
      console.error("Broker reported error:", frame.headers.message, frame.body);
    };

    client.activate();
    stompClientRef.current = client;

    return () => {
      setIsConnected(false);
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
    };
  }, [sessionId]);

  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [messages, isOpen]);

  const sendMessage = (event: FormEvent) => {
    event.preventDefault();
    if (!input.trim() || !sessionId || !stompClientRef.current?.active) {
      return;
    }

    const message: ChatMessage = {
      senderId: sessionId,
      senderName: tx("Khách hàng", "Customer"),
      content: input.trim(),
      isAdmin: false,
    };

    stompClientRef.current.publish({
      destination: "/app/chat",
      body: JSON.stringify(message),
    });

    setInput("");
  };

  return (
    <div className="chat-float">
      <button className="chat-float__button" onClick={() => setIsOpen((current) => !current)} type="button">
        {isOpen ? <X size={26} /> : <MessageCircle size={26} />}
      </button>

      {isOpen ? (
        <div className="chat-window">
          <div className="chat-window__header">
            <MessageCircle size={20} />
            <div>
              <h4>{tx("Hỗ trợ trực tuyến", "Support chat")}</h4>
              <p>{tx("Thông thường chúng tôi sẽ phản hồi trong vài phút.", "Reply time is usually just a few minutes.")}</p>
            </div>
          </div>

          <div className="chat-window__body">
            {!isBootstrapped ? (
              <div className="chat-window__state">
                <p>{tx("Đang tạo phiên hỗ trợ cho bạn...", "Creating your support session...")}</p>
              </div>
            ) : !isConnected ? (
              <div className="chat-window__state">
                <p>{tx("Đang kết nối tới hỗ trợ trực tuyến...", "Connecting to live support...")}</p>
              </div>
            ) : messages.length === 0 ? (
              <div className="chat-window__state">
                <p>{tx("Hãy cho chúng tôi biết bạn cần gì, đội hỗ trợ sẽ tiếp nhận ngay.", "Tell us what you need and we will pick it up from the admin desk.")}</p>
              </div>
            ) : (
              messages.map((message, index) => {
                const isMine = message.senderId === sessionId && message.isAdmin !== true;

                return (
                  <div key={`${message.timestamp ?? "message"}-${index}`} className={`chat-message ${isMine ? "chat-message--mine" : ""}`}>
                    {!isMine ? <span className="chat-message__sender">{message.senderName}</span> : null}
                    <div className="chat-message__bubble">{message.content}</div>
                  </div>
                );
              })
            )}

            <div ref={messagesEndRef} />
          </div>

          <div className="chat-window__footer">
            <form className="chat-form" onSubmit={sendMessage}>
              <input
                className="field"
                type="text"
                value={input}
                onChange={(event) => setInput(event.target.value)}
                placeholder={tx("Nhập tin nhắn của bạn...", "Type your message...")}
              />
              <button
                className="button button-primary"
                type="submit"
                disabled={!input.trim() || !isBootstrapped || !isConnected}
                aria-label={tx("Gửi tin nhắn", "Send message")}
              >
                <Send size={18} />
              </button>
            </form>
          </div>
        </div>
      ) : null}
    </div>
  );
}
