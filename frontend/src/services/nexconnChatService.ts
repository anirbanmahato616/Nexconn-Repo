
import {
    BaseChannel,
    DirectChannel,
    DirectChannelIdentifier,
    MessageHandler,
    NCEngine,
    SendTextMessageParams,
} from "@nexconn/chat";

// Message type used by the frontend
export interface ChatMessage {
  messageId: string;
  senderUserId: string;
  text: string;
  sentTime: number;
  messageType: string;
}

// Ensure Nexconn is connected before chat operations
const ensureConnected = () => {
  const currentUserId = NCEngine.getCurrentUserId();

  if (!currentUserId) {
    throw new Error("Nexconn is not connected. Please log in again.");
  }

  return currentUserId;
};

// Convert SDK message into our frontend message format
const mapMessage = (message: {
  messageId: string;
  senderUserId: string;
  content: { text?: string };
  sentTime: number;
  messageType: string;
}): ChatMessage => ({
  messageId: message.messageId,
  senderUserId: message.senderUserId,
  text: message.content?.text ?? "",
  sentTime: message.sentTime,
  messageType: message.messageType,
});

// Open a direct conversation with another Nexconn user
export const openDirectChannel = (otherUserId: string) => {
  ensureConnected();

  if (!otherUserId) {
    throw new Error("Recipient Nexconn user ID is missing.");
  }

  return new DirectChannel(otherUserId);
};

// Load previous messages
export const loadMessages = async (
  otherUserId: string,
  pageSize = 50
): Promise<ChatMessage[]> => {
  ensureConnected();

  const query = BaseChannel.createMessagesQuery({
    channelIdentifier: new DirectChannelIdentifier(otherUserId),
    pageSize,
    isAscending: true,
  });

  const result = await query.loadNextPage();

  if (!result.isOk || !result.data) {
    throw new Error("Unable to load conversation messages.");
  }

  return result.data.data.map(mapMessage);
};

// Send a text message
export const sendTextMessage = async (
  otherUserId: string,
  text: string
): Promise<ChatMessage> => {
  ensureConnected();

  const trimmedText = text.trim();

  if (!trimmedText) {
    throw new Error("Message cannot be empty.");
  }

  const channel = new DirectChannel(otherUserId);

  const result = await channel.sendMessage(
    new SendTextMessageParams({
      text: trimmedText,
    })
  );

  if (!result.isOk || !result.data) {
    throw new Error("Unable to send message.");
  }

  return mapMessage(result.data);
};

// Subscribe to incoming messages from a particular user
export const subscribeToMessages = (
  otherUserId: string,
  onMessage: (message: ChatMessage) => void
): (() => void) => {
  ensureConnected();

  const handlerId =
    `recoverx-chat-${otherUserId}-${Date.now()}-${Math.random()}`;

  const handler = new MessageHandler({
    onMessageReceived: ({ messages }) => {
      messages.forEach((message) => {
        if (message.senderUserId !== otherUserId) {
          return;
        }

        if (message.messageType !== "RC:TxtMsg") {
          return;
        }

        onMessage(mapMessage(message));
      });
    },
  });

  NCEngine.addMessageHandler(handlerId, handler);

  // Return cleanup function
  return () => {
    NCEngine.removeMessageHandler(handlerId);
  };
};
