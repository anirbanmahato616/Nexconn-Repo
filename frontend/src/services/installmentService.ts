import type {
    Installment,
    InstallmentRequest,
} from "../types/installment";

import api from "./api";

export const getInstallments = async (
  businessId: number,
  commitmentId: number
): Promise<Installment[]> => {
  const response = await api.get<Installment[]>(
    `/api/businesses/${businessId}/commitments/${commitmentId}/installments`
  );

  return response.data;
};

export const createInstallment = async (
  businessId: number,
  request: InstallmentRequest
): Promise<Installment> => {
  const response = await api.post<Installment>(
    `/api/businesses/${businessId}/commitments/installments`,
    request
  );

  return response.data;
};