
import type { Payment } from "../types/payment";
import api from "./api";

export interface PaymentClaimRequest {
  invoiceId: number;
  amount: number;
  paymentDate: string;
  paymentMethod: string;
  transactionReference: string;
}

// BUSINESS MODE

export const getBusinessPayments = async (
  businessId: number
): Promise<Payment[]> => {
  const response = await api.get<Payment[]>(
    `/api/businesses/${businessId}/payments`
  );

  return response.data;
};

export const verifyPayment = async (
  businessId: number,
  paymentId: number
): Promise<Payment> => {
  const response = await api.patch<Payment>(
    `/api/businesses/${businessId}/payments/${paymentId}/verify`,
    {}
  );

  return response.data;
};

export const rejectPayment = async (
  businessId: number,
  paymentId: number,
  reason: string
): Promise<Payment> => {
  const response = await api.patch<Payment>(
    `/api/businesses/${businessId}/payments/${paymentId}/reject`,
    { reason }
  );

  return response.data;
};

// CUSTOMER MODE

export const getMyPayments = async (): Promise<Payment[]> => {
  const response = await api.get<Payment[]>(
    "/api/customers/me/payments"
  );

  return response.data;
};

export const submitMyPaymentClaim = async (
  businessId: number,
  request: PaymentClaimRequest
): Promise<Payment> => {
  const response = await api.post<Payment>(
    `/api/customers/me/payments/${businessId}`,
    request
  );

  return response.data;
};
