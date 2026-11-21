
import type { Customer } from "../types/customer";
import api from "./api";

export interface CustomerRequest {
  name: string;
  email: string;
  phone: string;
}

export interface InvitationResponse {
  message: string;
  businessId: number;
  customerId: number;
  invitedEmail: string;
  expiresAt: string;
}

export const getBusinessCustomers = async (
  businessId: number
): Promise<Customer[]> => {
  const response = await api.get<Customer[]>(
    `/api/businesses/${businessId}/customers`
  );
  return response.data;
};

export const createCustomer = async (
  businessId: number,
  request: CustomerRequest
): Promise<Customer> => {
  const response = await api.post<Customer>(
    `/api/businesses/${businessId}/customers`,
    request
  );
  return response.data;
};

export const updateCustomer = async (
  businessId: number,
  customerId: number,
  request: CustomerRequest
): Promise<Customer> => {
  const response = await api.put<Customer>(
    `/api/businesses/${businessId}/customers/${customerId}`,
    request
  );
  return response.data;
};

// Send customer invitation
export const inviteCustomer = async (
  businessId: number,
  customerId: number
): Promise<InvitationResponse> => {
  const response = await api.post<InvitationResponse>(
    `/api/businesses/${businessId}/customers/${customerId}/invite`
  );

  return response.data;
};

// Accept customer invitation
export const acceptCustomerInvitation = async (
  businessId: number,
  token: string
): Promise<InvitationResponse> => {
  const response = await api.post<InvitationResponse>(
    `/api/businesses/${businessId}/customers/invitations/accept`,
    { token }
  );

  return response.data;
};
