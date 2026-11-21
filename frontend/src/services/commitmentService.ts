
import type {
  Commitment,
  CustomerCommitment,
} from "../types/commitment";

import api from "./api";

// BUSINESS MODE

export const getBusinessCommitments = async (
  businessId: number
): Promise<Commitment[]> => {
  const response = await api.get<Commitment[]>(
    `/api/businesses/${businessId}/commitments`
  );

  return response.data;
};

export const approveCommitment = async (
  businessId: number,
  commitmentId: number,
  note: string = ""
): Promise<Commitment> => {
  const response = await api.patch<Commitment>(
    `/api/businesses/${businessId}/commitments/${commitmentId}/approve`,
    { note }
  );

  return response.data;
};

export const rejectCommitment = async (
  businessId: number,
  commitmentId: number,
  note: string
): Promise<Commitment> => {
  const response = await api.patch<Commitment>(
    `/api/businesses/${businessId}/commitments/${commitmentId}/reject`,
    { note }
  );

  return response.data;
};

// CUSTOMER MODE

export const getMyCommitments = async (): Promise<CustomerCommitment[]> => {
  const response = await api.get<CustomerCommitment[]>(
    "/api/customers/me/commitments"
  );

  return response.data;
};
