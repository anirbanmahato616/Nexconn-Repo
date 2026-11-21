
import api from "./api";

// Business Mode conversation response
export interface BusinessConversation {
    id: number;
    businessId: number;
    invoiceId: number | null;
    ownerUserId: number;
    customerId: number;
    customerUserId: number;
    nexconnChannelId: string;
    createdAt: string;
}

// Customer Mode conversation response
export interface CustomerConversation {
    id: number;
    businessId: number;
    invoiceId: number | null;
    ownerUserId: number;
    nexconnChannelId: string;
    ownerNexconnUserId: string;
    businessName: string;
    businessEmail: string;
    createdAt: string;
}

// Response when creating or retrieving a direct conversation
export interface DirectConversationResponse {
    id: number;
    businessId: number;
    customerId: number;
    ownerUserId: number;
    customerUserId: number;
    invoiceId: number | null;
    channelId: string;
    otherUserNexconnId: string;
    currentUserNexconnId: string;
    createdAt: string;
}

// Connected business contact for Customer Mode
export interface CustomerBusinessContact {
    businessId: number;
    businessName: string;
    businessEmail: string;
}

// Get conversations for a business
export const getBusinessConversations = async (
    businessId: number
): Promise<BusinessConversation[]> => {
    const response = await api.get<BusinessConversation[]>(
        `/api/businesses/${businessId}/conversations`
    );

    return response.data;
};

// Get conversations for the logged-in customer
export const getCustomerConversations = async (): Promise<
    CustomerConversation[]
> => {
    const response = await api.get<CustomerConversation[]>(
        "/api/customers/me/conversations"
    );

    return response.data;
};

// Get all connected businesses for the logged-in customer
export const getMyBusinessContacts = async (): Promise<
    CustomerBusinessContact[]
> => {
    const response = await api.get<CustomerBusinessContact[]>(
        "/api/customers/me/conversations/businesses"
    );

    return response.data;
};

// Create or retrieve a direct conversation from Business Mode
export const createDirectConversation = async (
    businessId: number,
    customerId: number,
    invoiceId?: number
): Promise<DirectConversationResponse> => {
    const response = await api.post<DirectConversationResponse>(
        `/api/businesses/${businessId}/conversations/direct`,
        {
            customerId,
            ...(invoiceId !== undefined && { invoiceId }),
        }
    );

    return response.data;
};

// Create or retrieve a direct conversation from Customer Mode
export const createCustomerDirectConversation = async (
    businessId: number
): Promise<DirectConversationResponse> => {
    const response = await api.post<DirectConversationResponse>(
        "/api/customers/me/conversations/direct",
        { businessId }
    );

    return response.data;
};
