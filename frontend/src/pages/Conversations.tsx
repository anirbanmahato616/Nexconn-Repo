
import {
    useCallback,
    useEffect,
    useRef,
    useState,
    type FormEvent,
} from "react";

import { useOutletContext } from "react-router-dom";

import {
    Building2,
    Loader2,
    MessageCircle,
    Send,
    User,
} from "lucide-react";

import { NCEngine } from "@nexconn/chat";

import type { Business } from "../types/business";
import type { Customer } from "../types/customer";

import { getBusinessCustomers } from "../services/customerService";

import {
    createCustomerDirectConversation,
    createDirectConversation,
    getMyBusinessContacts,
    type DirectConversationResponse,
} from "../services/conversationService";

import { connectNexconn } from "../services/nexconnService";

import {
    loadMessages,
    sendTextMessage,
    subscribeToMessages,
    type ChatMessage,
} from "../services/nexconnChatService";

type DashboardContext = {
    mode: "business" | "customer";
    selectedBusiness: Business | null;
    businesses: Business[];
    loading: boolean;
};

type ContactItem = {
    key: string;
    businessId: number;
    customerId?: number;
    title: string;
    subtitle: string;
    canChat: boolean;
};

type ConversationItem = {
    id: number;
    businessId: number;
    invoiceId: number | null;
    title: string;
    subtitle: string;
    otherUserNexconnId: string;
    createdAt: string;
};

function getErrorMessage(error: any): string {
    const data = error.response?.data;

    if (typeof data === "string") return data;
    if (data?.message) return data.message;

    return error.message || "Something went wrong. Please try again.";
}

function formatTime(timestamp: number): string {
    if (!timestamp) return "";

    return new Date(timestamp).toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
    });
}

export default function Conversations() {
    const { mode, selectedBusiness } =
        useOutletContext<DashboardContext>();

    const [contacts, setContacts] = useState<ContactItem[]>([]);

    const [selectedContactKey, setSelectedContactKey] =
        useState<string | null>(null);

    const [selectedConversation, setSelectedConversation] =
        useState<ConversationItem | null>(null);

    const [messages, setMessages] = useState<ChatMessage[]>([]);

    const [pageLoading, setPageLoading] = useState(true);
    const [messagesLoading, setMessagesLoading] = useState(false);
    const [sending, setSending] = useState(false);
    const [connected, setConnected] = useState(false);

    const [creatingContactKey, setCreatingContactKey] =
        useState<string | null>(null);

    const [draft, setDraft] = useState("");
    const [error, setError] = useState("");

    const messagesEndRef = useRef<HTMLDivElement>(null);

    // Merge messages and prevent duplicate entries.
    const mergeMessages = useCallback(
        (previous: ChatMessage[], incoming: ChatMessage[]) => {
            const messageMap = new Map<string, ChatMessage>();

            previous.forEach((message) => {
                messageMap.set(message.messageId, message);
            });

            incoming.forEach((message) => {
                messageMap.set(message.messageId, message);
            });

            return Array.from(messageMap.values()).sort(
                (a, b) => a.sentTime - b.sentTime
            );
        },
        []
    );

    // Connect to Nexconn and load all connected contacts.
    useEffect(() => {
        let cancelled = false;

        const initialize = async () => {
            try {
                setPageLoading(true);
                setError("");
                setConnected(false);

                setContacts([]);
                setSelectedContactKey(null);
                setSelectedConversation(null);
                setMessages([]);

                if (mode === "business" && !selectedBusiness) {
                    return;
                }

                // Initialize Nexconn.
                await connectNexconn();

                if (cancelled) return;

                setConnected(true);

                // =====================================
                // BUSINESS MODE
                // =====================================
                if (mode === "business" && selectedBusiness) {
                    const customerData =
                        await getBusinessCustomers(selectedBusiness.id);

                    if (cancelled) return;

                    const mapped: ContactItem[] = customerData.map(
                        (customer: Customer) => ({
                            key: `customer-${customer.id}`,
                            businessId: selectedBusiness.id,
                            customerId: customer.id,

                            title: customer.name || "Customer",

                            subtitle:
                                customer.email || "Customer contact",

                            // A customer must accept the invitation
                            // before they can participate in Nexconn chat.
                            canChat: customer.userId !== null &&
                                customer.userId !== undefined,
                        })
                    );

                    setContacts(mapped);
                }

                // =====================================
                // CUSTOMER MODE
                // =====================================
                else {
                    const businessData = await getMyBusinessContacts();

                    if (cancelled) return;

                    const mapped: ContactItem[] = businessData.map(
                        (business) => ({
                            key: `business-${business.businessId}`,
                            businessId: business.businessId,

                            title:
                                business.businessName ||
                                `Business #${business.businessId}`,

                            subtitle:
                                business.businessEmail ||
                                "Business contact",

                            canChat: true,
                        })
                    );

                    setContacts(mapped);
                }
            } catch (err: any) {
                console.error(
                    "CONVERSATIONS INITIALIZATION FAILED:",
                    err
                );

                if (!cancelled) {
                    setError(getErrorMessage(err));
                }
            } finally {
                if (!cancelled) {
                    setPageLoading(false);
                }
            }
        };

        initialize();

        return () => {
            cancelled = true;
        };
    }, [mode, selectedBusiness?.id]);

    // Create or retrieve a conversation when a contact is selected.
    const handleSelectContact = async (contact: ContactItem) => {
        if (!contact.canChat || creatingContactKey) {
            return;
        }

        try {
            setCreatingContactKey(contact.key);
            setSelectedContactKey(contact.key);
            setError("");

            setMessages([]);
            setDraft("");
            setSelectedConversation(null);

            let response: DirectConversationResponse;

            // =====================================
            // BUSINESS MODE
            // =====================================
            if (mode === "business") {
                if (!contact.customerId) {
                    throw new Error("Customer information is unavailable.");
                }

                response = await createDirectConversation(
                    contact.businessId,
                    contact.customerId
                );
            }

            // =====================================
            // CUSTOMER MODE
            // =====================================
            else {
                response = await createCustomerDirectConversation(
                    contact.businessId
                );
            }

            // Map API response to the existing chat structure.
            const conversation: ConversationItem = {
                id: response.id,
                businessId: response.businessId,
                invoiceId: response.invoiceId,

                title: contact.title,
                subtitle: contact.subtitle,

                otherUserNexconnId: response.otherUserNexconnId,

                createdAt: response.createdAt,
            };

            setSelectedConversation(conversation);

        } catch (err: any) {
            console.error("DIRECT CONVERSATION FAILED:", err);

            setSelectedConversation(null);
            setError(getErrorMessage(err));
        } finally {
            setCreatingContactKey(null);
        }
    };

    // Load message history and subscribe to incoming messages.
    useEffect(() => {
        if (!connected || !selectedConversation) return;

        let cancelled = false;

        setMessages([]);
        setMessagesLoading(true);
        setError("");

        const cleanup = subscribeToMessages(
            selectedConversation.otherUserNexconnId,
            (incomingMessage) => {
                if (cancelled) return;

                setMessages((previous) =>
                    mergeMessages(previous, [incomingMessage])
                );
            }
        );

        const fetchHistory = async () => {
            try {
                const history = await loadMessages(
                    selectedConversation.otherUserNexconnId
                );

                if (!cancelled) {
                    setMessages((previous) =>
                        mergeMessages(previous, history)
                    );
                }
            } catch (err: any) {
                if (!cancelled) {
                    setError(getErrorMessage(err));
                }
            } finally {
                if (!cancelled) {
                    setMessagesLoading(false);
                }
            }
        };

        fetchHistory();

        return () => {
            cancelled = true;
            cleanup();
        };
    }, [
        connected,
        selectedConversation?.id,
        selectedConversation?.otherUserNexconnId,
        mergeMessages,
    ]);

    // Scroll to the latest message.
    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({
            behavior: "smooth",
        });
    }, [messages]);

    // Send message.
    const handleSendMessage = async (
        event: FormEvent<HTMLFormElement>
    ) => {
        event.preventDefault();

        if (!selectedConversation || !draft.trim() || sending) {
            return;
        }

        const messageText = draft.trim();

        try {
            setSending(true);
            setError("");
            setDraft("");

            const sentMessage = await sendTextMessage(
                selectedConversation.otherUserNexconnId,
                messageText
            );

            setMessages((previous) =>
                mergeMessages(previous, [sentMessage])
            );
        } catch (err: any) {
            setDraft(messageText);
            setError(getErrorMessage(err));
        } finally {
            setSending(false);
        }
    };

    // Initial loading.
    if (pageLoading) {
        return (
            <div className="flex items-center justify-center min-h-[400px]">
                <Loader2
                    className="animate-spin text-blue-600"
                    size={30}
                />

                <span className="ml-3 text-slate-500">
                    Loading contacts...
                </span>
            </div>
        );
    }

    // No business selected.
    if (mode === "business" && !selectedBusiness) {
        return (
            <div className="bg-white border rounded-xl p-8 text-center">
                <Building2
                    size={36}
                    className="mx-auto text-slate-400"
                />

                <h2 className="font-semibold text-lg mt-3">
                    No business selected
                </h2>

                <p className="text-sm text-slate-500 mt-1">
                    Select a business to view its connected customers.
                </p>
            </div>
        );
    }

    return (
        <div className="h-[calc(100vh-120px)] min-h-[500px] flex flex-col">

            {/* Page heading */}
            <div className="mb-5">
                <h1 className="text-2xl font-bold text-slate-900">
                    Conversations
                </h1>

                <p className="text-sm text-slate-500 mt-1">
                    {mode === "business"
                        ? "Communicate with your connected customers."
                        : "Communicate with the businesses you are connected to."}
                </p>
            </div>

            {/* Main workspace */}
            <div className="flex-1 min-h-0 bg-white border border-slate-200 rounded-xl overflow-hidden flex">

                {/* Contact list */}
                <aside className="w-full sm:w-80 border-r border-slate-200 flex flex-col shrink-0">

                    <div className="p-4 border-b border-slate-200">
                        <h2 className="font-semibold text-slate-900">
                            {mode === "business" ? "Customers" : "Businesses"}
                        </h2>

                        <p className="text-xs text-slate-500 mt-1">
                            {contacts.length} connected contact(s)
                        </p>
                    </div>

                    <div className="flex-1 overflow-y-auto">

                        {contacts.length === 0 ? (
                            <div className="p-6 text-center">
                                <MessageCircle
                                    size={32}
                                    className="mx-auto text-slate-300"
                                />

                                <p className="text-sm font-medium mt-3 text-slate-700">
                                    No connected contacts
                                </p>

                                <p className="text-xs text-slate-500 mt-1">
                                    Connected contacts will appear here.
                                </p>
                            </div>
                        ) : (
                            contacts.map((contact) => (
                                <button
                                    key={contact.key}
                                    onClick={() => handleSelectContact(contact)}
                                    disabled={
                                        !contact.canChat ||
                                        creatingContactKey !== null
                                    }
                                    className={`w-full text-left p-4 border-b border-slate-100 transition ${
                                        selectedContactKey === contact.key
                                            ? "bg-blue-50"
                                            : "hover:bg-slate-50"
                                    } ${
                                        !contact.canChat
                                            ? "opacity-60 cursor-not-allowed"
                                            : ""
                                    }`}
                                >
                                    <div className="flex items-center gap-3">

                                        <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-700 shrink-0">
                                            {mode === "business" ? (
                                                <User size={20} />
                                            ) : (
                                                <Building2 size={20} />
                                            )}
                                        </div>

                                        <div className="min-w-0 flex-1">
                                            <p className="font-semibold text-sm text-slate-900 truncate">
                                                {contact.title}
                                            </p>

                                            <p className="text-xs text-slate-500 truncate mt-1">
                                                {contact.subtitle}
                                            </p>

                                            {!contact.canChat && (
                                                <p className="text-xs text-amber-600 mt-1">
                                                    Invitation not accepted
                                                </p>
                                            )}
                                        </div>

                                        {creatingContactKey === contact.key && (
                                            <Loader2
                                                size={18}
                                                className="animate-spin text-blue-600 shrink-0"
                                            />
                                        )}
                                    </div>
                                </button>
                            ))
                        )}

                    </div>
                </aside>

                {/* Chat window */}
                <section className="flex-1 min-w-0 flex flex-col">

                    {selectedConversation ? (
                        <>

                            {/* Chat header */}
                            <div className="p-4 border-b border-slate-200 flex items-center gap-3">

                                <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-700">
                                    {mode === "business" ? (
                                        <User size={20} />
                                    ) : (
                                        <Building2 size={20} />
                                    )}
                                </div>

                                <div className="min-w-0">
                                    <h2 className="font-semibold text-slate-900 truncate">
                                        {selectedConversation.title}
                                    </h2>

                                    <p className="text-xs text-slate-500">
                                        {mode === "business" ? "Customer" : "Business"}
                                    </p>
                                </div>
                            </div>

                            {/* Error */}
                            {error && (
                                <div className="mx-4 mt-3 bg-red-50 border border-red-200 text-red-600 rounded-lg p-3 text-sm">
                                    {error}
                                </div>
                            )}

                            {/* Messages */}
                            <div className="flex-1 overflow-y-auto p-4 sm:p-6 bg-slate-50">

                                {messagesLoading ? (
                                    <div className="flex justify-center items-center h-full text-slate-500">
                                        <Loader2
                                            size={22}
                                            className="animate-spin mr-2"
                                        />
                                        Loading messages...
                                    </div>
                                ) : messages.length === 0 ? (
                                    <div className="flex flex-col justify-center items-center h-full text-center">
                                        <MessageCircle
                                            size={40}
                                            className="text-slate-300"
                                        />

                                        <p className="font-medium text-slate-700 mt-3">
                                            No messages yet
                                        </p>

                                        <p className="text-sm text-slate-500 mt-1">
                                            Send a message to start the conversation.
                                        </p>
                                    </div>
                                ) : (
                                    <div className="space-y-4">
                                        {messages.map((message) => {
                                            const isOwnMessage =
                                                message.senderUserId ===
                                                NCEngine.getCurrentUserId();

                                            return (
                                                <div
                                                    key={message.messageId}
                                                    className={`flex ${
                                                        isOwnMessage
                                                            ? "justify-end"
                                                            : "justify-start"
                                                    }`}
                                                >
                                                    <div
                                                        className={`max-w-[80%] sm:max-w-[70%] rounded-2xl px-4 py-2.5 shadow-sm ${
                                                            isOwnMessage
                                                                ? "bg-blue-600 text-white rounded-br-sm"
                                                                : "bg-white text-slate-800 border border-slate-200 rounded-bl-sm"
                                                        }`}
                                                    >
                                                        <p className="text-sm whitespace-pre-wrap break-words">
                                                            {message.text}
                                                        </p>

                                                        <p
                                                            className={`text-[10px] text-right mt-1 ${
                                                                isOwnMessage
                                                                    ? "text-blue-100"
                                                                    : "text-slate-400"
                                                            }`}
                                                        >
                                                            {formatTime(message.sentTime)}
                                                        </p>
                                                    </div>
                                                </div>
                                            );
                                        })}

                                        <div ref={messagesEndRef} />
                                    </div>
                                )}
                            </div>

                            {/* Message input */}
                            <form
                                onSubmit={handleSendMessage}
                                className="p-3 sm:p-4 border-t border-slate-200 flex items-center gap-2"
                            >
                                <input
                                    type="text"
                                    value={draft}
                                    onChange={(event) =>
                                        setDraft(event.target.value)
                                    }
                                    placeholder="Type your message..."
                                    disabled={sending || !connected}
                                    className="flex-1 min-w-0 border border-slate-300 rounded-full px-4 py-3 text-sm outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-slate-100"
                                />

                                <button
                                    type="submit"
                                    disabled={
                                        !draft.trim() ||
                                        sending ||
                                        !connected
                                    }
                                    className="w-11 h-11 rounded-full bg-blue-600 hover:bg-blue-700 text-white flex items-center justify-center disabled:bg-blue-300 shrink-0"
                                >
                                    {sending ? (
                                        <Loader2
                                            size={18}
                                            className="animate-spin"
                                        />
                                    ) : (
                                        <Send size={18} />
                                    )}
                                </button>
                            </form>

                        </>
                    ) : (
                        <div className="flex-1 flex flex-col items-center justify-center text-center p-6">

                            <MessageCircle
                                size={48}
                                className="text-slate-300"
                            />

                            <h2 className="font-semibold text-slate-700 mt-4">
                                Select a contact
                            </h2>

                            <p className="text-sm text-slate-500 mt-1">
                                Choose a {mode === "business" ? "customer" : "business"} from the left to start chatting.
                            </p>

                            {error && (
                                <div className="mt-4 bg-red-50 border border-red-200 text-red-600 rounded-lg p-3 text-sm">
                                    {error}
                                </div>
                            )}

                        </div>
                    )}
                </section>
            </div>
        </div>
    );
}
