
import { NCEngine } from "@nexconn/chat";
import api from "./api";

export interface NexconnTokenResponse {
  userId: string;
  accessToken: string;
}

let isInitialized = false;
let connectionPromise: Promise<string> | null = null;

// Fetch Nexconn token from RecoverX backend
export const getNexconnToken = async (): Promise<NexconnTokenResponse> => {
  const response = await api.post<NexconnTokenResponse>(
    "/api/nexconn/token"
  );

  return response.data;
};

// Initialize Nexconn SDK
const initializeNexconn = () => {
  if (isInitialized) return;

  const appKey = import.meta.env.VITE_NEXCONN_APP_KEY;

  if (!appKey) {
    throw new Error("Nexconn App Key is missing.");
  }

  NCEngine.initialize({
    appKey,
  });

  isInitialized = true;
};

// Connect the logged-in RecoverX user to Nexconn
export const connectNexconn = async (): Promise<string> => {
  if (connectionPromise) {
    return connectionPromise;
  }

  connectionPromise = (async () => {
    initializeNexconn();

    const tokenResponse = await getNexconnToken();

    // Reuse the existing connection if it belongs to this user
    const currentUserId = NCEngine.getCurrentUserId();

    if (currentUserId === tokenResponse.userId) {
      return currentUserId;
    }

    // Disconnect if another user is currently connected
    if (currentUserId) {
      await NCEngine.disconnect();
    }

    const result = await NCEngine.connect({
      token: tokenResponse.accessToken,
    });

    if (!result.isOk || !result.data?.userId) {
  throw new Error("Failed to connect to Nexconn.");
}

return result.data.userId;
  })();

  try {
    return await connectionPromise;
  } finally {
    connectionPromise = null;
  }
};

// Disconnect Nexconn
export const disconnectNexconn = async (): Promise<void> => {
  connectionPromise = null;

  if (NCEngine.getCurrentUserId()) {
    await NCEngine.disconnect();
  }
};
