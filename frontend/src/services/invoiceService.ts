
import type { Invoice } from "../types/invoice";
import api from "./api";

export interface InvoiceRequest {
  customerId: number;
  invoiceNumber: string;
  description: string;
  amount: number;
  issueDate: string;
  dueDate: string;
}

// BUSINESS MODE

export const getBusinessInvoices = async (
  businessId: number
): Promise<Invoice[]> => {
  const response = await api.get<Invoice[]>(
    `/api/businesses/${businessId}/invoices`
  );

  return response.data;
};

export const createInvoice = async (
  businessId: number,
  request: InvoiceRequest
): Promise<Invoice> => {
  const response = await api.post<Invoice>(
    `/api/businesses/${businessId}/invoices`,
    request
  );

  return response.data;
};

export const getInvoiceById = async (
  businessId: number,
  invoiceId: number
): Promise<Invoice> => {
  const response = await api.get<Invoice>(
    `/api/businesses/${businessId}/invoices/${invoiceId}`
  );

  return response.data;
};

export const updateInvoice = async (
  businessId: number,
  invoiceId: number,
  request: InvoiceRequest
): Promise<Invoice> => {
  const response = await api.put<Invoice>(
    `/api/businesses/${businessId}/invoices/${invoiceId}`,
    request
  );

  return response.data;
};

// CUSTOMER MODE

export const getMyInvoices = async (): Promise<Invoice[]> => {
  const response = await api.get<Invoice[]>(
    "/api/customers/me/invoices"
  );

  return response.data;
};

export const getMyInvoiceById = async (
  invoiceId: number
): Promise<Invoice> => {
  const response = await api.get<Invoice>(
    `/api/customers/me/invoices/${invoiceId}`
  );

  return response.data;
};
